import java.awt.desktop.SystemSleepEvent;
import java.io.File;
import java.util.*;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

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
        String temp = "Worker End Health State\n" + "IP:" + ip + "\n" + "CPU: " + String.valueOf(CPU) + "%\n" + "Memory: " + String.valueOf(memory) + "%\n" + "Active: " + active + "\n" + "Requests in queue: " + String.valueOf(requests);
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
    private static Boolean alreadyCreateExtraInstance = true;

    public static void main(String[] args) {
        //Cloud PaaS = new Cloud();
        //openstack.createServer();
        //PaaS.ListServers(PaaS.masterOS);
        //openstack.deleteServer();
        //openstack.ListFlavors();
        //openstack.ListImages();


        /*for(Object server:PaaS.servers) {
            System.out.println(server);
            //need to change after test
        }*/

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
                    workerEnd = Storage.workerEndList.get("120.0.0.1");

                    alreadyCreateExtraInstance = true;
                }else if(workerAllBusy){
                    //assign to first work end
                    Iterator<String> ips = Storage.workerIPSet.iterator();
                    if(ips.hasNext()) {
                        workerEnd = Storage.workerEndList.get(ips.next());
                    }
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
