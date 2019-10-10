
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import java.nio.file.Files;
import java.nio.file.Paths;

//import Lastfinding.scalability.*;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;



class User extends Thread{
    private String id = "Earth";
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private Boolean keepConnection = true;

    //receive and print message from server
    private String receiveAndPrintMessageFromServer(BufferedReader in) {
        String temp = "";
        while (true) {
            try {
                String resp = in.readLine();
                if(resp.equals(DefaultKeys.MESSAGE_END_FLAG)) {
                    break;
                }
                else {
                    temp = resp;
                    System.out.println(resp);
            }
            } catch (IOException e) {
                System.out.println("Can't receive message from server!\nError Details: ");
                e.printStackTrace();
            }
        }
        return temp;
    }

    //receive parameter from server
    private List receiveParametersMessageFromServer(BufferedReader in) {
        List parameters = new ArrayList<String>();
        while (true) {
            try {
                String resp = in.readLine();
                if(resp.equals(DefaultKeys.MESSAGE_END_FLAG)) {
                    break;
                }
                else {
                    parameters.add(resp);
                }
            } catch (IOException e) {
                System.out.println("Can't receive parameters from server!\nError Details: ");
                e.printStackTrace();
            }
        }
        return parameters;
    }

    //handle the message from server
    private String handleDataFromServerAndMakeRespond(BufferedReader in, PrintWriter out) {
        String resp_type = null;
        String resp = null;
        try {
            resp_type = in.readLine();
        } catch (IOException e) {
            System.out.println("Disconnect from server!\nDetails: ");
            e.printStackTrace();
            keepConnection = false;
            return "Error";
        }

        if(resp_type == null) {
            System.out.println("Disconnect from server!");
            keepConnection = false;
            return "Error";
        }

        switch (resp_type) {
            case DefaultKeys.DISPLAY_MESSAGE_FLAG:
                receiveAndPrintMessageFromServer(in);
                break;
            case DefaultKeys.INQUIRY_MESSAGE_FLAG:
                String temp = receiveAndPrintMessageFromServer(in);

                //send reply message to server
                if(!temp.equals(DefaultKeys.pressEnterToContinueMessage)) {
                    System.out.println("Send to Server:");
                }
                Scanner scanner = new Scanner(System.in);
                String input = scanner.nextLine();
                out.println(input);

                if(input.toUpperCase().equals(DefaultKeys.CLOSE_CLIENT_FLAG)){
                    keepConnection = false;
                }
                break;
            case DefaultKeys.PERMIT_UPLOAD_FLAG:
                if(true){//(uploadFile(parameters[0],parameters[1])) {
                    out.println(DefaultKeys.END_UPLOAD_FILE_SUCCEED);
                }else {
                    out.println(DefaultKeys.END_UPLOAD_FILE_FAILED);
                }
                break;

            case DefaultKeys.PERMIT_DOWNLOAD_FLAG:
                List<String> parameters = receiveParametersMessageFromServer(in);
                List<String> savePaths = new ArrayList<String>();
                for (String filePath:parameters) {
                    File file = new File("WorkDir/" + filePath.split("/")[filePath.split("/").length - 2]);
                    if(!file.exists()) {
                        file.mkdir();
                    }
                    savePaths.add("WorkDir/" + filePath.split("/")[filePath.split("/").length - 2] + "/" +  filePath.split("/")[filePath.split("/").length - 1]);
                }
                if(FileIO.downloadFile(DefaultKeys.masterIP,"key.pem",parameters, savePaths)) {
                    System.out.println("Succeed received output files!");
                }else {
                    System.out.println("Failed received output files!");
                }
                break;

            default:
                resp_type = "UNKNOWN_TYPE";
        }
        return resp_type;
    }

    User(InetAddress addr, int port) {
        System.out.println("Starting client: " + id);
        /*get current system time*/
        long startTime =  System.currentTimeMillis();
        //start socket
        try {
            socket = new Socket(addr, port);
            long endTime =  System.currentTimeMillis();
            long usedTime = (endTime-startTime);/*second*/
            System.out.println("Connnection time: " + usedTime + " ms");
            start();
        }
        catch (Exception e) {
            // If the creation of the socket fails,
            // nothing needs to be cleaned up.
            System.out.println("Can't connect to server!\nError Details: " + e.toString());
        }
    }

    public void run() {
        try {
            System.out.println("socket = " + socket);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Output is automatically flushed by PrintWriter:
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);

            while (keepConnection) {
                String resp = handleDataFromServerAndMakeRespond(in,out);
            }

        } catch (Exception e) {
            System.out.println("Error Details: ");
            e.printStackTrace();
        } finally {
            System.out.println("closing client...");
            try {
                if(socket != null) {
                    socket.close();
                }
            } catch (Exception e) {
                System.out.println("Can't close client socket!\nError Details: ");
                e.printStackTrace();
            }
        }
    }
}

public class UserEnd {
    private static String SERVER_IP = "";
    private static int PORT;

    public static void main(String[] args) {
        SERVER_IP = DefaultKeys.masterIP;//"115.146.84.200";//"144.6.227.102";
        PORT = DefaultKeys.clientPort;

        try {
            InetAddress addr = InetAddress.getByName(SERVER_IP);
            new User(addr, PORT);
        }catch(UnknownHostException e) {
            System.out.println("Can't get server address!\nError Details: ");
            e.printStackTrace();
        }
    }
}