import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.*;
import java.util.*;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

class runCommand extends Thread{
    String host;
    String user;
    String privateKey;
    String command;

    runCommand(String ip, String key,String command) {
        host = ip;
        user = "ubuntu";
        privateKey = key;
        this.command = command;
        start();
    }

    public void run() {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            Properties config = new Properties();
            //session.setPassword("KIT318");
            jsch.addIdentity(privateKey);
            System.out.println("identity added ");
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            Channel channel = session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);

            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);

            channel.setInputStream(System.in);
            channel.connect();

            /*InputStream input = channel.getInputStream();
            try {
            BufferedReader br = new BufferedReader(new InputStreamReader(input));
            String line;
            while ((line = br.readLine()) != null) {
            System.out.println(line);
            }

            } catch (IOException io) {
            System.out.println("Exception occurred during reading file from SFTP server due to " + io.getMessage());
            io.getMessage();

            } catch (Exception e) {
            System.out.println("Exception occurred during reading file from SFTP server due to " + e.getMessage());
            e.getMessage();
            }*/
        }catch(Exception e){
            System.out.println(e);
        }
    }
}

class WorkerEndAdapter {
    String ip;
    String active;

    boolean isBusy;

    int CPU;
    int memory;
    int requests;

    ServerOneSecretary secretary;
    ServerOneWorker worker;

    WorkerEndAdapter(String p) {
        ip = p;
        isBusy = false;
    }

    public void print() {
        String temp = "\nWorker End Health State\n" + "IP:" + ip + "\n" + "CPU: " + String.valueOf(CPU) + "%\n" + "Memory: " + String.valueOf(memory) + "%\n" + "Active: " + active + "\n" + "Requests in queue: " + String.valueOf(requests) + "\n";
        System.out.println(temp);
    }
}

class Storage {
    public static ReentrantLock jLock = new ReentrantLock();
    public static HashMap<String, Job> jobList = new HashMap(); //所有的工作列表
    public static Queue<Job> jobQueue = new LinkedList<Job>(); //待分配队列

    public static ReentrantLock wLock = new ReentrantLock();
    public static Set workerIPSet = new HashSet();
    public static HashMap<String, WorkerEndAdapter> workerEndList = new HashMap();
}

public class MasterEnd {
    private static Boolean workerAllBusy = false;
    private static Boolean alreadyCreateExtraInstance = false;

    public static void main(String[] args) {
        System.out.println("Initial PaaS...");
        Cloud PaaS = new Cloud();
        PaaS.ListServers(true, null);

        //local test code
        try {
            WorkerEndAPMonitor weAdapter = new WorkerEndAPMonitor();
        } catch (Exception e) {
            System.out.println("Failed to start Worker End AP Monitor");
            e.printStackTrace();
            System.exit(0);
        }

    //Start Client AP Monitor
        try {
            ClientAPMonitor cAPM = new ClientAPMonitor();
        } catch (Exception e) {
            System.out.println("Failed to start Client AP Monitor");
            e.printStackTrace();
            System.exit(0);
        }

        while(true) {
            try {
                TimeUnit.SECONDS.sleep(1);
                //System.out.println(Storage.jobQueue);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //System.out.println(Storage.jobQueue);
            if(!Storage.jobQueue.isEmpty()) {
                WorkerEndAdapter workerEnd = null;
                workerAllBusy = true;
                //choose one work end assign job
                System.out.println("Assign job");
                for (WorkerEndAdapter end: Storage.workerEndList.values()) {
                    if(!end.isBusy) {
                        workerAllBusy = false;
                        workerEnd  = end;
                        break;
                    }
                }
                //create new instance
                if(workerAllBusy && !alreadyCreateExtraInstance) {
                    String ip = PaaS.CreateServer();
                    if(ip != null) {
                        System.out.println("Trying to start Worker End...");
                        try {
                            TimeUnit.SECONDS.sleep(60);
                            //System.out.println(Storage.jobQueue);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        new runCommand(ip, DefaultKeys.privateKey,"java -jar /home/ubuntu/PaaS/WorkerEnd.jar");
                    }else {
                        System.out.println("Failed started Worker End");
                    }

                    try {
                        TimeUnit.SECONDS.sleep(10);
                        //System.out.println(Storage.jobQueue);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    workerEnd = Storage.workerEndList.get(ip);

                    alreadyCreateExtraInstance = true;
                }else if(workerAllBusy){
                    //assign work randomly
                    int max = Storage.workerEndList.size();
                    int num=(int)(Math.random()* max);//生成[0,size]之间的随机整数
                    List <String> tempL = new ArrayList<String>(Storage.workerIPSet);
                    try {
                        workerEnd = Storage.workerEndList.get(tempL.get(num));
                        System.out.println("All Worker Ends are busy, randomly choose Worker End:" + num);
                        System.out.println(Storage.workerEndList);
                    }catch (Exception e) {
                        System.out.println("");
                    }

                    /*
                    //assign to first work end
                    Iterator<String> ips = Storage.workerIPSet.iterator();
                    if(ips.hasNext()) {
                        workerEnd = Storage.workerEndList.get(ips.next());
                    }
                    */
                }

                if(workerEnd != null){
                    Job newJob = Storage.jobQueue.poll();

                    File dir = new File(DefaultKeys.workDir + newJob.ID);
                    dir.mkdir();

                    Storage.jLock.lock();
                    newJob.secretaryForWorker = workerEnd.secretary;
                    workerEnd.secretary.jobQueue.offer(newJob);
                    Storage.jobList.get(newJob.ID).state = "Waiting for execution";
                    Storage.jLock.unlock();
                }else {
                    System.out.println("WARNING! \nNo worker is connected by master! \nAll job will be hang on until master connect to a worker.");
                }
            }
        }

    }
}
