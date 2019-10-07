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

    private boolean keepConnection = true;

    public static void main(String[] args) {
        WorkBook workBook = new WorkBook();

        try {
            InetAddress addr = InetAddress.getByName(DefaultKeys.masterIP);
            new Worker(addr, DefaultKeys.workerPort, workBook);
            new Secretary(addr, DefaultKeys.secretaryPort, workBook);
        }catch(Exception e) {
            System.out.println("Can't start Worker End!\nError Details: ");
            e.printStackTrace();
        }
    }
}
