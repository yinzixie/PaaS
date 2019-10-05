import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

class ServerOneWorker extends Thread {

    public ServerOneWorker(InetAddress addr, int port, String com) throws Exception {

        start();
    }

    public void run() {
    }
}