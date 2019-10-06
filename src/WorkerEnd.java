import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

class WorkBook {
    public ReentrantLock workBookLock = new ReentrantLock();
    public Queue<Job> jobQueue = new LinkedList<Job>();
    public Queue<Job> cancelQueue = new LinkedList<Job>();
    public Queue<Job> jobStateQueue = new LinkedList<Job>();
    public Process currentProcess;
    public Job currentJob;
}

public class WorkerEnd {
    public int ID;

    public boolean isBusy;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private boolean keepConnection = true;

    public static void main(String[] args) {

        WorkBook workBook = new WorkBook();

        //Start ServerOneWorker AP Monitor
        try {
            ServerOneWorkerAPMonitor workerAPM = new ServerOneWorkerAPMonitor(workBook);
        } catch (Exception e) {
            System.out.println("Failed to start ServerOneWorker AP Monitor");
            e.printStackTrace();
            System.exit(0);
        }

        //Start ServerOneSecretary AP Monitor
        try {
            ServerOneSecretaryAPMonitor secretaryAPM = new ServerOneSecretaryAPMonitor(workBook);
        } catch (Exception e) {
            System.out.println("Failed to start ServerOneSecretary AP Monitor");
            e.printStackTrace();
            System.exit(0);
        }

        while (true) {

        }
    }
}
