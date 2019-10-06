import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

class ServerOneWorker extends Thread {
    InetAddress addr;
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

    ServerOneWorker(InetAddress ad, int port) throws Exception {
        addr = ad;
        System.out.println("Start Server One Worker: " + addr);
        /*get current system time*/
        long startTime =  System.currentTimeMillis();
        //start socket

        socket = new Socket(addr, port);
        long endTime =  System.currentTimeMillis();
        long usedTime = (endTime-startTime);/*second*/
        System.out.println("Server One Worker Connection time: " + usedTime + " ms");

        start();
    }

    public void run() {
        try {
            System.out.println("socket = " + socket);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Output is automatically flushed by PrintWriter:
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);

            while (keepConnection) {
                String resp = handleDataFromWorker(in,out);
            }
        } catch (Exception e) {
            System.out.println("Error Details: ");
            e.printStackTrace();
        } finally {
            System.out.println("closing Server One Worker: " + addr);
            closeConnection();
        }
    }
}