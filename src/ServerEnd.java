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

import java.util.List;

/*import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.image.Image;
import org.openstack4j.openstack.OSFactory;*/

class Job {
    public String ID = "id";
    public String time = "12:00";
    public String appFile = "appFile";
}

class Storage {
    public static HashMap jobList = new HashMap();

}

class ServerOneClient extends Thread{
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private boolean keepConnection = true;

    private static ReentrantLock lock = new ReentrantLock();

    private void sendInquiryMessageToClient(PrintWriter out, String inquiry) {
        out.println(DefaultKeys.INQUIRY_MESSAGE_FLAG);
        out.println(inquiry);
        out.println(DefaultKeys.MESSAGE_END_FLAG);
    }

    private void sendParameterMessageToClient(PrintWriter out, String parameter) {
        out.println(DefaultKeys.PARAMETER_MESSAGE_FLAG);
        out.println(parameter);
        //out.println(DefaultKeys.MESSAGE_END_FLAG);
    }

    private void sendDisplayMessageToClient(PrintWriter out, String message) {
        out.println(DefaultKeys.DISPLAY_MESSAGE_FLAG);
        out.println(message);
        out.println(DefaultKeys.MESSAGE_END_FLAG);
    }

    private void sendUploadFilePermitMessageToClient(PrintWriter out,String parameters) {
        out.println(DefaultKeys.UPLOAD_FILE_FLAG);
        out.println(parameters);
    }

    private String receiveClientReplyParameter(BufferedReader in) {
        String resp = null;
        try {
            resp = in.readLine();
            System.out.println("Received message from client: " + socket + " : " + resp);
        } catch (IOException e) {
            System.out.println("Can't receive message from client!\nError Details: ");
            e.toString();
        }
        return resp;
    }

    private String generateJobID() {
        String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random=new Random();
        StringBuffer sb=new StringBuffer();

        for(int i=0;i<8;i++){
            int number=random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    //close connection from client (which means close the socket in code)
    private void closeConnection() {
        System.out.println("Trying to close the connection: " + socket);
        try {
            socket.close();
            System.out.println("Connection closed succeed: " + socket);
        }
        catch(IOException e2) {
            System.out.println("Connection can't be closed: " + socket + " : " + e2.toString());
        }
    }

    public ServerOneClient(Socket s) throws IOException {
        socket = s;

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        // Enable auto-flush:
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

        start();
    }

    private  void startANewJob(String[] parameters) {
        if(parameters.length != 5) {
            sendDisplayMessageToClient(out, "Missing parameters!");
        }else{
            String appType = parameters[1];
            String inputFile = parameters[2];
            String remotePath = parameters[3];
            String time = parameters[4];

            //check type
            if(appType.equalsIgnoreCase("jar") || appType.equalsIgnoreCase("py")) {
                //check file type
                String suffix = inputFile.substring(inputFile.lastIndexOf(".") + 1);
                if(suffix.equalsIgnoreCase(appType)) {
                    sendUploadFilePermitMessageToClient(out,inputFile + " " + remotePath );

                    //receive files
                    if(receiveClientReplyParameter(in).equalsIgnoreCase(DefaultKeys.END_UPLOAD_FILE_SUCCEED)) {
                        //generate passcode = id
                        String passcode = "";
                        do {
                            passcode = generateJobID();
                        }while( Storage.jobList.get(passcode)!=null);

                        Job newJob = new Job();
                        newJob.ID = passcode;
                        newJob.appFile = remotePath;
                        newJob.time = time;

                        //save job
                        lock.lock();
                        Storage.jobList.put(passcode, newJob);
                        lock.unlock();

                        //send message to client
                        sendDisplayMessageToClient(out, "Your Job Passcode:" + passcode);

                        //assign job




                    }
                }else {
                    sendDisplayMessageToClient(out, "Inconsistent application type!");
                }
            }else {
                sendDisplayMessageToClient(out, "Nonsupport application type!");
            }
        }
    }

    public void run() {
        while(keepConnection) {
            //send initial message to client
            sendInquiryMessageToClient(out, DefaultKeys.initialMessage);
            String optionStr = receiveClientReplyParameter(in);

            //receive operation choice
            if(optionStr == null) {
                keepConnection = false;
                closeConnection();
            }else {
                String[] arrayStr = optionStr.split(" ");
                String firstStr = arrayStr[0];
                // option
                if (firstStr.toUpperCase().equals(DefaultKeys.startOption)) {
                    startANewJob(arrayStr);
                }else if (firstStr.toUpperCase().equals(DefaultKeys.checkOption)) {
                    sendDisplayMessageToClient(out, "check ....asd");

                }else if (firstStr.toUpperCase().equals(DefaultKeys.cancleOption)) {
                    sendDisplayMessageToClient(out, "cancle ....asd");

                }else if (firstStr.toUpperCase().equals(DefaultKeys.receiveOption)) {
                    sendDisplayMessageToClient(out, "receive ....asd");
                }else {
                    sendDisplayMessageToClient(out, DefaultKeys.wrongOptionMessage);
                }
                sendInquiryMessageToClient(out, "Press enter to continue...");
                receiveClientReplyParameter(in);
            }

        }
    }

}




public class ServerEnd
{
    static int PORT = 8080;
    private ReentrantLock mLock = new ReentrantLock();

   /* OSClientV3 os=null;
    public ServerEnd() {
        os = OSFactory.builderV3()
                .endpoint("https://keystone.rc.nectar.org.au:5000/v3")
                .credentials("username", "secret",Identifier.byName("domainName"))
                .scopeToProject(Identifier.byId("projectid"))
                .authenticate();
    }

    public void createServer() {
        ServerCreate server = Builders.server()
                .name("Ubuntu 2")
                .flavor("XXX")
                .image("YYYY")
                .keypairName("ZZZZ")
                .build();

        os.compute().servers().boot(server);
    }

    //List of all flavors
    public void ListFlavors() {

        List<Flavor> flavors = (List<Flavor>) os.compute().flavors().list();
        System.out.println(flavors);
    }
    //List of all images
    public void ListImages() {

        List<? extends Image> images = (List<? extends Image>) os.compute().images().list();
        System.out.println(images);
    }
    //List of all Servers
    public void ListServers() {

        List<? extends Server> servers = os.compute().servers().list();
        System.out.println(servers);
    }
    //Delete a Server
    public void deleteServer() {

        os.compute().servers().delete("serverId");
    }*/


    public static void main(String[] args )
    {
       /* ServerEnd openstack=new ServerEnd();
        //openstack.createServer();
        openstack.ListServers();
        openstack.ListFlavors();
        openstack.ListImages();*/

       try {
           ServerSocket s = new ServerSocket(PORT);
           System.out.println("Server Started");
           try {
               while (true) {
                   // Blocks until a connection occurs:
                   Socket socket = s.accept();
                   System.out.println("Connection accepted: " + socket);
                   try {
                       new ServerOneClient(socket);
                   } catch (IOException e) {
                       // If it fails, close the socket,
                       // otherwise the thread will close it:
                       System.out.println("Can't start server!\nDetails: ");
                       e.toString();
                       socket.close();
                   }
               }
           } catch (IOException e) {
               System.out.println("Error Details: ");
               e.toString();
           } finally {
               s.close();
           }
       }catch (IOException e) {
           System.out.println("Server Failed to start!");
           System.out.println("Error Details: ");
           e.toString();
       }
    }
}
