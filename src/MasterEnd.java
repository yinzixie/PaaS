import java.util.*;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

class Storage {
    public static ReentrantLock jLock = new ReentrantLock();
    public static HashMap<String, Job> jobList = new HashMap(); //所有的工作列表
    public static Queue<Job> jobQueue = new LinkedList<Job>(); //待分配队列

    public static ReentrantLock sLock = new ReentrantLock();
    public static List<ServerOneSecretary> secretaryList = new ArrayList<ServerOneSecretary>(); //每个worker对应的秘书

    public static Queue<ServerOneSecretary> waitQueue = new LinkedList<ServerOneSecretary>(); //请求工作队列
}

public class MasterEnd {




    public static void main(String[] args) {
       // Cloud PaaS = new Cloud();
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
            InetAddress addr = InetAddress.getByName(DefaultKeys.masterIP);
            WorkerEndAdapter weAdapter = new WorkerEndAdapter(addr, DefaultKeys.workerPort, DefaultKeys.secretaryPort);
        } catch (Exception e) {
            System.out.println("Failed to start Server One Worker/Secretary: " + DefaultKeys.masterIP);
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
                ServerOneSecretary secretary;
                System.out.println("Receive request and assign job");
                if(Storage.waitQueue.isEmpty()) {
                    secretary = Storage.secretaryList.get(0);
                }else {
                    secretary = Storage.waitQueue.poll();
                }

                Job newJob = Storage.jobQueue.poll();
                Storage.jLock.lock();
                newJob.secretaryForWorker = secretary;
                secretary.jobQueue.offer(newJob);
                Storage.jobList.get(newJob.ID).state = "Waiting for execution";
                Storage.jLock.unlock();
            }
        }

    }
}
