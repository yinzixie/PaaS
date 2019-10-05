import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WorkerAPMonitor {
    public WorkerAPMonitor() throws IOException {
        System.out.println("Start Worker AP Monitor...");
        ServerSocket s = new ServerSocket(DefaultKeys.clientPort);

        try {
            while (true) {
                // Blocks until a connection occurs:
                Socket socket = s.accept();
                System.out.println("Worker Connection accepted: " + s);
                try {
                    new ServerOneClient(socket);
                } catch (IOException e) {
                    // If it fails, close the socket,
                    // otherwise the thread will close it:
                    System.out.println("Worker AP Monitor Failed to start!\nError Details:");
                    e.toString();
                    socket.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Error Details: ");
            e.toString();
        } finally {
            s.close();
        }
    }
}
