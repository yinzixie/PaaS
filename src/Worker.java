import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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
        String command;
        String cmd;
        String appName;
        if(job.appType == "jar") {
            cmd = "java -jar";
            appName = "app.jar";
        }else {
            cmd = "python";
            appName = "app.py";
        }

        File dir = new File(job.ID);
        dir.mkdir();

        String appFilePath = appName;//dir + "/" + appName;
        String inputFilePath = "input.txt";//dir + "/" + "input.txt";

        //copy

        List<String> cmdList = Arrays.asList(cmd, appFilePath, inputFilePath);
        System.out.println(cmdList);
        System.out.println("Creating Process");

        ProcessBuilder builder = new ProcessBuilder(cmdList);

        builder.redirectErrorStream(true);// 重定向错误输出流到正常输出流

        //builder.directory(dir);
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
        sendJobStateToMaster(out, job, DefaultKeys.jobSucceed);
    }

    public Worker(Socket s, WorkBook book) throws IOException {
        socket = s;
        workBook = book;
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
                System.out.println("finished");
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
