import org.openstack4j.model.compute.Server;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class WorkerEndAdapter extends Thread{
    InetAddress addr;
    ServerOneSecretary secretary;
    ServerOneWorker worker;

    public WorkerEndAdapter(InetAddress ad, int workerPort, int secretaryPort) throws Exception {
        addr =ad;
        System.out.println("Strat Worker End Adapter: " + addr);
        worker = new ServerOneWorker(addr, workerPort);
        secretary = new ServerOneSecretary(addr, secretaryPort);

        Storage.sLock.lock();
        Storage.secretaryList.add(secretary);
        Storage.sLock.unlock();

        start();
    }

    public void run() {
        while(true) {
            try {
                Thread.currentThread().sleep(10000); //10s一次循环检查连接
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try{
                secretary.socket.sendUrgentData(0xFF);
                worker.socket.sendUrgentData(0xFF);
            }catch(Exception ex){
                Storage.sLock.lock();
                Storage.secretaryList.remove(secretary);
                Storage.sLock.unlock();

                if(secretary.jobList.size() > 0) {
                    for(Job job:secretary.jobList){
                        if(Storage.jobList.get(job.ID).state != DefaultKeys.jobSucceed) {
                            Storage.jLock.lock();
                            Storage.jobQueue.offer(job);
                            Storage.jLock.unlock();
                        }
                    }
                }

                secretary.closeConnection();
                worker.closeConnection();
                System.out.println("Disconnect from Worker End: " + addr + "\nError Details:");
                ex.printStackTrace();
                break;
            }
        }

    }
}
