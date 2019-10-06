import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientAPMonitor extends Thread{
    ServerSocket s;

    public ClientAPMonitor() throws Exception {
        System.out.println("Start Client AP Monitor...");
        s = new ServerSocket(DefaultKeys.clientPort);
        start();
    }

    public void run(){
        try {
            while (true) {
                // Blocks until a connection occurs:
                Socket socket = s.accept();
                System.out.println("Client Connection accepted: " + s);
                try {
                    new ServerOneClient(socket);
                } catch (Exception e) {
                    // If it fails, close the socket,
                    // otherwise the thread will close it:
                    System.out.println("Client AP Monitor Failed to start!\nError Details:");
                    e.toString();
                    socket.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Error Details: ");
            e.toString();
        } finally {
            try {
                s.close();
            } catch (IOException e) {
                System.out.println("Client AP Monitor Failed to close!\nError Details:");
                e.printStackTrace();
            }
        }
    }
}
