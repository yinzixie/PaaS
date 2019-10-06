import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

class ServerOneSecretary extends Thread {
    InetAddress addr;
    Socket socket;
    BufferedReader in;
    PrintWriter out;

    Boolean keepConnection  =true;

    ReentrantLock jLock = new ReentrantLock();

    List<Job> jobList = new ArrayList<Job>();
    Queue<Job> jobQueue = new LinkedList<Job>();
    Queue<Job> cancelQueue = new LinkedList<Job>();

    boolean queryWorkerHealthy = false;

    public void closeConnection(){
        keepConnection = false;
        try {
            if(socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            System.out.println("Can't close Server One Secretary!\nError Details: ");
            e.printStackTrace();
        }
    }

    private void sendJobToSecretary(PrintWriter out,Job job) {
        out.println(DefaultKeys.ASSIGN_JOB_MESSAGE_FLAG);
        out.println(job.ID);
        out.println(job.appFile);
        out.println(job.inputFile);
        out.println(job.time);
    }

    /*public String getJobStateFromSecretary(BufferedReader in, PrintWriter out) throws Exception {
        out.println(DefaultKeys.GET_JOB_STATE_FLAG);
        String resp = in.readLine();
        return resp;
    }*/

    public void getWorkerStateFromSecretary(BufferedReader in, PrintWriter out) throws IOException {
        out.println(DefaultKeys.GET_WORKER_STATE_FLAG);
        while (true) {
            String resp = in.readLine();
            if(resp.equals(DefaultKeys.MESSAGE_END_FLAG)) {
                break;
            }
            else {
                System.out.println(resp);
            }
        }
    }

    public void cancelJobToSecretary(PrintWriter out, Job job) {
        out.println(DefaultKeys.CANCLE_JOB_FLAG);
        out.println(job.ID);
    }

    public ServerOneSecretary(InetAddress ad, int port) throws Exception {
        addr = ad;
        System.out.println("Start Server One Secretary: " + addr);
        /*get current system time*/
        long startTime =  System.currentTimeMillis();
        //start socket

        socket = new Socket(addr, port);
        long endTime =  System.currentTimeMillis();
        long usedTime = (endTime-startTime);/*second*/
        System.out.println("Server One Secretary Connection time: " + usedTime + " ms");

        start();
    }

    public void run() {
        int period = 5;
        int sec = 0;
        try {
            System.out.println("socket = " + socket);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Output is automatically flushed by PrintWriter:
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);

            while (keepConnection) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                    sec++;
                    if(sec >= period) {
                        queryWorkerHealthy = true;
                        sec = 0;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (socket.isClosed()) {
                    keepConnection = false;
                }else {
                    if(!jobQueue.isEmpty()) {
                        jLock.lock();
                        Job newJob = jobQueue.poll();
                        jLock.unlock();

                        jobList.add(newJob);
                        sendJobToSecretary(out, newJob);
                    }
                    if(!cancelQueue.isEmpty()) {
                        jLock.lock();
                        Job cancelJob = cancelQueue.poll();
                        jLock.unlock();

                        cancelJobToSecretary(out, cancelJob);
                        //jobList.remove(cancelJob);
                    }
                    if(queryWorkerHealthy) {
                        queryWorkerHealthy = false;
                        getWorkerStateFromSecretary(in, out);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error Details: ");
            e.printStackTrace();
        } finally {
            System.out.println("Close Server One Secretary: " + addr);
            closeConnection();
        }
    }
}