import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

class ServerOneWorker extends Thread {
    Socket socket;
    BufferedReader in;
    PrintWriter out;

    Boolean keepConnection  =true;

    private void receiveJobStateFromWorker(BufferedReader in) throws Exception {
        String id = in.readLine();
        String state = in.readLine();

        Storage.jLock.lock();
        Storage.jobList.get(id).state = state;
        Storage.jLock.unlock();

        if(state.equals(DefaultKeys.jobFailed)) {
            //re assign job
            Storage.jLock.lock();
            Storage.jobQueue.offer(Storage.jobList.get(id));
            Storage.jLock.unlock();
        }
    }

    public void closeConnection(){
        keepConnection = false;
        try {
            if(socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            System.out.println("Can't close Server One Worker!\nError Details: ");
            e.printStackTrace();
        }
    }

    private String handleDataFromWorker(BufferedReader in, PrintWriter out) throws Exception {
        String resp = in.readLine();
        if(resp.equals(DefaultKeys.JOB_STATE_MESSAGE_FLAG)) {
            receiveJobStateFromWorker(in);
        }

        return resp;
    }

    ServerOneWorker(Socket s) throws Exception {
        socket = s;
        System.out.println("Start Server One Worker: " + socket);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        // Output is automatically flushed by PrintWriter:
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
        start();
    }

    public void run() {
        try {
            while (keepConnection) {
                String resp = handleDataFromWorker(in,out);
            }
        } catch (Exception e) {
            System.out.println("Error Details: ");
            e.printStackTrace();
        } finally {
            System.out.println("closing Server One Worker: " + socket);
            closeConnection();
        }
    }
}