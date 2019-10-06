import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerOneSecretaryAPMonitor extends Thread {
    private ServerSocket s;
    private WorkBook workBook;

    ServerOneSecretaryAPMonitor(WorkBook book) throws Exception {
        System.out.println("Start ServerOneSecretary AP Monitor...");
       // System.out.println(book.test);
        workBook = book;
        s = new ServerSocket(DefaultKeys.secretaryPort);

        start();
    }

    public void run() {
        try {
            while (true) {
                // Blocks until a connection occurs:
                Socket socket = s.accept();
                System.out.println("ServerOneSecretary Connection accepted: " + s);
                try {
                    new Secretary(socket, workBook);
                } catch (Exception e) {
                    // If it fails, close the socket,
                    // otherwise the thread will close it:
                    System.out.println("ServerOneSecretary AP Monitor Failed to start!\nError Details:");
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
                System.out.println("ServerOneSecretary AP Monitor Failed to close!\nError Details:");
                e.printStackTrace();
            }
        }
    }
}
