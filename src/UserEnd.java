
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Properties;

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
    private void receiveAndPrintMessageFromServer(BufferedReader in) {
        String resp = null;
        while (true) {
            try {
                resp = in.readLine();
                if(resp.equals(DefaultKeys.MESSAGE_END_FLAG)) {
                    break;
                }
                else {
                    System.out.println(resp);
            }
            } catch (IOException e) {
                System.out.println("Can't receive message from server!\nError Details: ");
                e.printStackTrace();
            }
        }
    }

    //receive parameter from server
    private String receiveParameterMessageFromServer(BufferedReader in) {
        String resp = null;
        try {
            resp = in.readLine();
        } catch (IOException e) {
            System.out.println("Can't receive message from server!\nError Details: ");
            e.printStackTrace();
        }
        return resp;
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

        //display message
        if(resp_type.equals(DefaultKeys.DISPLAY_MESSAGE_FLAG)) {
            receiveAndPrintMessageFromServer(in);
        }

        //inquiry message
        else if(resp_type.equals(DefaultKeys.INQUIRY_MESSAGE_FLAG)) {
            receiveAndPrintMessageFromServer(in);

            //send reply message to server
            Scanner scanner = new Scanner(System.in);
            System.out.println("Send to Server:");
            String input = scanner.nextLine();

            out.println(input);

            if(input.toUpperCase().equals(DefaultKeys.CLOSE_CLIENT_FLAG)){
                keepConnection = false;
            }
        }

        //parameter message
        else if(resp_type.equals(DefaultKeys.PARAMETER_MESSAGE_FLAG)) {
            resp = receiveParameterMessageFromServer(in);
            return resp;
        }

        else if(resp_type.equals(DefaultKeys.UPLOAD_FILE_FLAG)) {
            resp = receiveParameterMessageFromServer(in);
            String[] parameters = resp.split(" ");
            if(true){//(uploadFile(parameters[0],parameters[1])) {
                out.println(DefaultKeys.END_UPLOAD_FILE_SUCCEED);
            }else {
                out.println(DefaultKeys.END_UPLOAD_FILE_FAILED);
            }
        }

        //unknown message
        else {
            resp_type = "UNKNOWN_TYPE";
        }
        return resp_type;
    }

    private Boolean uploadFile(String src, String dst) {
        try {
            String user = "ubuntu";
            String privateKey = "D:\\University of Tasmania\\2019\\Semester 2\\KIT318 Big Data and Cloud Computing\\key.pem";
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, DefaultKeys.masterIP, 22);
            Properties config = new Properties();
            //session.setPassword("KIT418@utas"); ////if password is empty please comment it
            jsch.addIdentity(privateKey);

            System.out.println("Start uploading file " + src + " to " +  dst + " ...");

            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;

            sftpChannel.put(src, dst, ChannelSftp.OVERWRITE);

            sftpChannel.exit();
            session.disconnect();
        } catch (JSchException e) {
            System.out.println("Upload Failed!\nError Details:");
            e.printStackTrace();
            return false;
        } catch (SftpException e) {
            System.out.println("Upload Failed!\nError Details:");
            e.printStackTrace();
            return false;
        }
        catch(Exception e){
            System.out.println("Upload Failed!\nError Details:");
            System.out.println(e);
            return false;
        }
        return true;
    }

    public static void downloadFile() {

        try {
            String host = "144.6.227.102";
            String user = "ubuntu";
            String privateKey = "D:\\University of Tasmania\\2019\\Semester 2\\KIT318 Big Data and Cloud Computing\\key.pem";
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            Properties config = new Properties();
            //session.setPassword("KIT418@utas"); ////if password is empty please comment it
            jsch.addIdentity(privateKey);
            System.out.println("identity added ");
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);;
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;

            BufferedWriter writer = new BufferedWriter(new FileWriter("D:/University of Tasmania/2019/Semester 2/KIT318 Big Data and Cloud Computing/Assignment 3/serveroutput.java", true)); //local path to store downloaded file

            InputStream stream = sftpChannel.get("/home/ubuntu/Output/text.txt"); //server file path
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                String line;
                while ((line = br.readLine()) != null) {
                    writer.append(line);
                }
                System.out.println("File Downloaded");
                writer.close();
            } catch (IOException io) {
                System.out.println("Exception occurred during reading file from SFTP server due to " + io.getMessage());
                io.getMessage();

            } catch (Exception e) {
                System.out.println("Exception occurred during reading file from SFTP server due to " + e.getMessage());
                e.getMessage();

            }

            sftpChannel.exit();
            session.disconnect();
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

    public User(InetAddress addr, int port) {
        System.out.println("Starting client: " + id);
        /*get current system time*/
        long startTime =  System.currentTimeMillis();
        //start socket
        try {
            socket = new Socket(addr, port);
            long endTime =  System.currentTimeMillis();
            long usedTime = (endTime-startTime);/*second*/
            System.out.println("Connnection time: " + usedTime + " ms");
        }
        catch (IOException e) {
            // If the creation of the socket fails,
            // nothing needs to be cleaned up.
            System.out.println("Can't connect to server!\nError Details: " + e.toString());
        }
        start();
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

        } catch (IOException e) {
            System.out.println("Error Details: ");
            e.printStackTrace();
        } finally {
            System.out.println("closing client...");
            try {
                if(socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
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