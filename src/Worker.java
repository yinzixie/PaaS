import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Worker extends Thread{
    private Socket socket;
    private PrintWriter out;
    WorkBook workBook;

    private boolean keepConnection = true;

    private void sendJobStateToMaster(PrintWriter out, Job job, String state) {
        out.println(DefaultKeys.JOB_STATE_MESSAGE_FLAG);
        out.println(job.ID);
        out.println(state);
    }


    public void startJob(Job job) throws Exception {
        String cmd;
        String appName;

        if(job.appType == "jar") {
            cmd = "java -jar";
            appName = "app.jar";
        }else {
            cmd = "python";
            appName = "app.py";
        }

        File dir = new File(DefaultKeys.workDir + job.ID);
        dir.mkdir();

        String appFilePath = dir + "/" + appName;
        String inputFilePath = dir + "/" + "input.txt";
        String outputFilePath = dir + "/" + "output.txt";

        //Download files
        System.out.println("Downloading files for job: " + job.ID);

        List<String> remoteSrcs = new ArrayList<String>();
        List<String> localDsts = new ArrayList<String>();

        remoteSrcs.add(job.appFile);
        localDsts.add(appFilePath);

        remoteSrcs.add(job.inputFile);
        localDsts.add(inputFilePath);

        if(FileIO.downloadFile(DefaultKeys.masterIP, DefaultKeys.privateKey, remoteSrcs, localDsts)) {
            List<String> cmdList = Arrays.asList(cmd, appFilePath, inputFilePath);
            System.out.println(cmdList);
            System.out.println("Creating Process");

            ProcessBuilder builder = new ProcessBuilder(cmdList);

            builder.redirectErrorStream(true);// 重定向错误输出流到正常输出流

            builder.directory(dir);
            sendJobStateToMaster(out, job, "In Execution");
            workBook.currentProcess = builder.start();
            workBook.currentJob = job;

            InputStream is = workBook.currentProcess.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            workBook.currentProcess.waitFor();

            //upload file
            System.out.println("Uploading files for job: " + job.ID);

            remoteSrcs = new ArrayList<String>();
            localDsts = new ArrayList<String>();

            remoteSrcs.add(outputFilePath);
            localDsts.add(outputFilePath);

            if(FileIO.uploadFile(DefaultKeys.masterIP, DefaultKeys.privateKey, localDsts, remoteSrcs)) {
                sendJobStateToMaster(out, job, DefaultKeys.jobSucceed);
            }else {
                sendJobStateToMaster(out, job, DefaultKeys.jobFailed);
            }
        }else {
            sendJobStateToMaster(out, job, DefaultKeys.jobFailed);
        }
    }

    public Worker(InetAddress addr, int port, WorkBook book) throws Exception {
        System.out.println("Starting Worker...");
        workBook = book;
        /*get current system time*/
        long startTime =  System.currentTimeMillis();
        //start socket
        socket = new Socket(addr, port);
        long endTime =  System.currentTimeMillis();
        long usedTime = (endTime-startTime);/*second*/
        System.out.println("Connection time: " + usedTime + " ms");

        // Enable auto-flush:
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        start();
    }

    public void run() {
        while (keepConnection) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!workBook.jobQueue.isEmpty()) {
                try {
                    workBook.workBookLock.lock();
                    Job j = workBook.jobQueue.poll();
                    workBook.workBookLock.unlock();
                    startJob(j);
                } catch (Exception e) {
                    System.out.println("Failed to start job\nError Details:");
                    e.printStackTrace();
                }
                System.out.println("Finished");
            }
            if(!workBook.cancelQueue.isEmpty()) {
                workBook.workBookLock.lock();
                Job j = workBook.cancelQueue.poll();
                workBook.workBookLock.unlock();
                System.out.println("Job canceled: " + j.ID);
                sendJobStateToMaster(out, j, DefaultKeys.jobCanceled);
            }
        }
    }
}
