import java.io.*;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.net.Socket;

public class Secretary extends Thread{
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    WorkBook workBook;

    Boolean keepConnection = true;

    private static OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    public static int cpuLoad() {
        double cpuLoad = osmxb.getSystemCpuLoad();
        int percentCpuLoad = (int) (cpuLoad * 100);
        return percentCpuLoad;
    }

    public static int memoryLoad() {
        double totalvirtualMemory = osmxb.getTotalPhysicalMemorySize();
        double freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize();

        double value = freePhysicalMemorySize/totalvirtualMemory;
        int percentMemoryLoad = (int) ((1-value)*100);
        return percentMemoryLoad;
    }

    private void sendWorkerStateToMaster(PrintWriter out, String state) {
        out.println(DefaultKeys.WORKER_STATE_MESSAGE_FLAG);
        out.println(state);
        out.println(DefaultKeys.MESSAGE_END_FLAG);
    }

    private void closeConnection() {
        keepConnection  = false;
        try {
            if(socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            System.out.println("Can't close Secretary!\nError Details: ");
            e.printStackTrace();
        }
    }

    public Secretary(Socket s, WorkBook book) throws Exception {
        workBook = book;
        socket = s;
        // Enable auto-flush:
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        start();
    }

    public void run() {
        while (keepConnection) {
            try {
                String resp = in.readLine();

                if(resp.equals(DefaultKeys.ASSIGN_JOB_MESSAGE_FLAG)) {
                    String id = in.readLine();
                    String appFile = in.readLine();
                    String inputFile = in.readLine();
                    String time = in.readLine();
                    Job newJob = new Job(id, appFile, inputFile, time);
                    System.out.println("Received job");
                    workBook.workBookLock.lock();
                    workBook.jobQueue.offer(newJob);
                    workBook.workBookLock.unlock();

                }else if(resp.equals(DefaultKeys.GET_WORKER_STATE_FLAG)) {
                    String active;
                    if(workBook.currentJob == null) {
                        active = "Sleeping";
                    }else {
                        active = "In Execution";
                    }
                    String healthy = "CPU: " + String.valueOf(cpuLoad()) + "%\n" + "Memory: " + String.valueOf(memoryLoad()) + "%\n" + "Active: " + active + "\n" + "Requests in queue: " + String.valueOf(workBook.jobQueue.size());
                    sendWorkerStateToMaster(out, healthy);

                }else if(resp.equals(DefaultKeys.CANCLE_JOB_FLAG)) {
                    String jobID = in.readLine();
                    if(workBook.currentJob.ID.equals(jobID)) {
                        workBook.workBookLock.lock();
                        workBook.currentProcess.destroy();
                        workBook.cancelQueue.offer(workBook.currentJob);
                        workBook.workBookLock.unlock();
                    }else {
                        for (Job x : workBook.jobQueue) {
                            if(x.ID.equals(jobID)) {
                                workBook.workBookLock.lock();
                                workBook.jobQueue.remove(x);
                                workBook.cancelQueue.offer(x);
                                workBook.workBookLock.unlock();
                                break;
                            }
                        }
                    }
                    System.out.println("Job Canceled: " + jobID);
                }
                /*else if(resp.equals(DefaultKeys.GET_JOB_STATE_FLAG)) {
                    String jobID = in.readLine();
                    try{
                        workBook.job.exitValue();
                        out.println();
                    }catch(IllegalThreadStateException e){//进程没有终止
                        out.println();
                    }

                }*/
            } catch (Exception e) {
                System.out.println("Disconnect from Master\nError Details:");
                e.printStackTrace();
                System.out.println("Close Secretary");
                closeConnection();
            }
        }
    }
}
