import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WorkerAPMonitor extends Thread {
    ServerSocket s;

    public WorkerAPMonitor() throws Exception {
        System.out.println("Start Worker AP Monitor...");
        s = new ServerSocket(DefaultKeys.workerPort);
        start();
    }

    public void run(){
        try {
            while (true) {
                // Blocks until a connection occurs:
                Socket socket = s.accept();
                System.out.println("Worker Connection accepted: " + s);
                try {
                    new ServerOneWorker(socket);
                } catch (Exception e) {
                    // If it fails, close the socket,
                    // otherwise the thread will close it:
                    System.out.println("Worker AP Monitor Failed to start!\nError Details:");
                    e.printStackTrace();
                    socket.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Error Details: ");
            e.printStackTrace();
        } finally {
            try {
                s.close();
            } catch (IOException e) {
                System.out.println("Worker AP Monitor Failed to close!\nError Details:");
                e.printStackTrace();
            }
        }
    }
}
