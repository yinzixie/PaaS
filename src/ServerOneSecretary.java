import org.openstack4j.model.compute.Server;

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

    ServerOneWorker worker;
    boolean queryWorkerHealthy = false;

    public void closeConnection(){
        Storage.wLock.lock();
        Storage.workerIPSet.remove(socket.getInetAddress().getHostAddress().toString());
        Storage.workerEndList.remove(socket.getInetAddress().getHostAddress().toString());
        Storage.wLock.unlock();

        if(this.jobList.size() > 0) {
            for(Job job:this.jobList){
                if(Storage.jobList.get(job.ID).state != DefaultKeys.jobSucceed || Storage.jobList.get(job.ID).state != DefaultKeys.jobCanceled) {
                    job.state = "Re Assigning...";
                    Storage.jLock.lock();
                    Storage.jobQueue.offer(job);
                    Storage.jLock.unlock();
                }
            }
        }
        System.out.println("All jobs have been transferred");

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

    private void getWorkerStateFromSecretary(BufferedReader in, PrintWriter out) throws IOException {
        out.println(DefaultKeys.GET_WORKER_STATE_FLAG);

        String start = in.readLine();
        String cpu = in.readLine();
        String memory = in.readLine();
        String active = in.readLine();
        String requests = in.readLine();
        String end = in.readLine();

        Storage.wLock.lock();
        WorkerEndAdapter workerEnd = Storage.workerEndList.get(socket.getInetAddress().getHostAddress().toString());
        workerEnd.CPU = Integer.valueOf(cpu);
        workerEnd.memory = Integer.valueOf(memory);
        workerEnd.active = active;
        workerEnd.requests = Integer.valueOf(requests);
        workerEnd.isBusy = workerEnd.requests > DefaultKeys.workerBusyThreshold;
        Storage.wLock.unlock();

        workerEnd.print();
    }

    public void cancelJobToSecretary(PrintWriter out, Job job) {
        out.println(DefaultKeys.CANCEL_JOB_FLAG);
        out.println(job.ID);
    }

    public ServerOneSecretary(Socket s) throws Exception {
        socket = s;
        System.out.println("Start Server One Secretary: " + socket);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        // Output is automatically flushed by PrintWriter:
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
        start();
    }

    public void run() {
        int period = 10;
        int sec = 0;
        try {
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

                        if(!jobList.contains(jobList)) {
                            jobList.add(newJob);
                        }
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
            this.closeConnection();
        }
    }
}