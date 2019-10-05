import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

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

    private  void startANewJob(String[] parameters) {
        if(parameters.length != 4) {
            sendDisplayMessageToClient(out, "Missing parameters!");
        }else{
            String inputFile = parameters[1];
            String remotePath = parameters[2];
            String time = parameters[3];

            String appType = inputFile.substring(inputFile.lastIndexOf(".") + 1);

            String command = remotePath + " " + inputFile;
            //check type
            if(appType.equalsIgnoreCase("jar") || appType.equalsIgnoreCase("py")) {
                sendUploadFilePermitMessageToClient(out,inputFile + " " + remotePath );

                //receive files
                if(receiveClientReplyParameter(in).equalsIgnoreCase(DefaultKeys.END_UPLOAD_FILE_SUCCEED)) {
                    //generate passcode = id
                    String passcode;
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

                    try {
                        InetAddress addr = InetAddress.getByName(DefaultKeys.masterIP);
                        new ServerOneWorker(addr, DefaultKeys.workerPort, command);
                    }catch(Exception e) {
                        System.out.println("Can't get worker address!\nError Details: ");
                        e.printStackTrace();

                        //re assignment



                    }

                }
            }else {
                sendDisplayMessageToClient(out, "Nonsupport application type!");
            }
        }
    }

    public ServerOneClient(Socket s) throws IOException {
        socket = s;

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        // Enable auto-flush:
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

        start();
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