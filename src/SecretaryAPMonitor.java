import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SecretaryAPMonitor extends Thread{
    ServerSocket s;

    public SecretaryAPMonitor() throws Exception {
        System.out.println("Start Secretary AP Monitor...");
        s = new ServerSocket(DefaultKeys.secretaryPort);
        start();
    }

    public void run(){
        try {
            while (true) {
                // Blocks until a connection occurs:
                Socket socket = s.accept();
                System.out.println("Secretary Connection accepted: " + s);
                try {
                    ServerOneSecretary secretary = new ServerOneSecretary(socket);

                    String ip = socket.getInetAddress().getHostAddress().toString();
                    System.out.println(ip);
                    Storage.wLock.lock();
                    Storage.workerIPSet.add(ip);
                    Storage.wLock.unlock();

                    WorkerEndAdapter adapter= new WorkerEndAdapter(ip);
                    adapter.secretary = secretary;

                    Storage.wLock.lock();
                    Storage.workerEndList.put(ip,adapter);
                    Storage.wLock.unlock();
                } catch (Exception e) {
                    // If it fails, close the socket,
                    // otherwise the thread will close it:
                    System.out.println("Secretary AP Monitor Failed to start!\nError Details:");
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
                System.out.println("Secretary AP Monitor Failed to close!\nError Details:");
                e.printStackTrace();
            }
        }
    }
}
