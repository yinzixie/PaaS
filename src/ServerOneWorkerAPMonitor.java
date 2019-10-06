import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerOneWorkerAPMonitor extends  Thread{
    private ServerSocket s;
    private WorkBook workBook;

    ServerOneWorkerAPMonitor(WorkBook book) throws Exception {
        System.out.println("Start ServerOneWorker AP Monitor...");

        workBook = book;
        s = new ServerSocket(DefaultKeys.workerPort);

        start();
    }

    public void run() {
        try {
            while (true) {
                // Blocks until a connection occurs:
                Socket socket = s.accept();
                System.out.println("ServerOneWorker Connection accepted: " + s);
                try {
                    new Worker(socket, workBook);
                } catch (Exception e) {
                    // If it fails, close the socket,
                    // otherwise the thread will close it:
                    System.out.println("ServerOneWorker AP Monitor Failed to start!\nError Details:");
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
                System.out.println("ServerOneWorker AP Monitor Failed to close!\nError Details:");
                e.printStackTrace();
            }
        }
    }
}
