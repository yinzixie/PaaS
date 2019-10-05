import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

//doing main job and report job state


public class WorkerEnd {
    public int ID;

    public  boolean isBusy;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private boolean keepConnection = true;

    public static void main(String[] args) {
        int id = 0;
        try {
            ServerSocket s = new ServerSocket(DefaultKeys.workerPort);
            System.out.println("Worker End Started");
            try {
                while (true) {
                    // Blocks until a connection occurs:
                    Socket socket = s.accept();
                    System.out.println("Connection accepted: " + socket);
                    try {
                        //new Worker(socket);
                    } catch (Exception e) {
                        // If it fails, close the socket,
                        // otherwise the thread will close it:
                        System.out.println("Can't start Worker End!\nDetails: ");
                        e.toString();
                        socket.close();
                    }
                }
            } catch (IOException e) {
                System.out.println("Error Details: ");
                e.toString();
            } finally {
                s.close();
            }
        }catch (IOException e) {
            System.out.println("Worker End Failed to start!");
            System.out.println("Error Details: ");
            e.toString();
        }
    }
}
