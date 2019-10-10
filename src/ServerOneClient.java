import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

class ServerOneClient extends Thread{
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private boolean keepConnection = true;

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
        out.println(DefaultKeys.PERMIT_UPLOAD_FLAG);
        out.println(parameters);
    }

    private void sendDownloadFilePermitMessageToClient(PrintWriter out,List<String> parameters) {
        out.println(DefaultKeys.PERMIT_DOWNLOAD_FLAG);
        for (String filePath:parameters) {
            out.println(filePath);
        }
        out.println(DefaultKeys.MESSAGE_END_FLAG);
    }

    private String receiveClientReplyParameter(BufferedReader in) {
        String resp = null;
        try {
            resp = in.readLine();
            System.out.println("Received message from client: " + socket + " : " + resp);
        } catch (IOException e) {
            System.out.println("Can't receive message from client!\nError Details: ");
            e.printStackTrace();
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

    private void startANewJob(String[] parameters) {
        if(parameters.length < 4) {
            sendDisplayMessageToClient(out, "Missing parameters!");
        }else{
            String appFile = parameters[1];
            String inputFile = parameters[2];
            String time = parameters[3];

            String appType = appFile.substring(appFile.lastIndexOf(".") + 1);

            //check type
            if(appType.equalsIgnoreCase("jar") || appType.equalsIgnoreCase("py")) {
                //check files
                File appfile = new File(appFile);
                File inputfile = new File(inputFile);

                if (!appfile.exists()) {
                    sendDisplayMessageToClient(out, "App doesn't exist!");
                }else {
                    if (!inputfile.exists()) {
                        sendDisplayMessageToClient(out, "Input file doesn't exist!");
                    }else {
                        //generate passcode = id
                        String passcode;
                        do {
                            passcode = generateJobID();
                        }while(Storage.jobList.get(passcode)!=null);

                        Job newJob = new Job(passcode, appFile, inputFile,time);

                        //save job
                        Storage.jLock.lock();
                        Storage.jobList.put(passcode, newJob);
                        Storage.jobQueue.offer(newJob);
                        Storage.jLock.unlock();

                        //send message to client
                        sendDisplayMessageToClient(out, "Your Job Passcode:" + passcode);
                    }
                }
            }else {
                sendDisplayMessageToClient(out, "Nonsupport application type!");
            }
        }
    }

    private void checkJobState(String id) {
        Job job = Storage.jobList.get(id);
        if(job != null) {
            sendDisplayMessageToClient(out, job.state);
        }else {
            sendDisplayMessageToClient(out,"Wrong Job ID!");
        }
    }

    private void cancelJob(String id) {
        Job job = Storage.jobList.get(id);
        if(job != null) {
            switch (job.state) {
                case DefaultKeys.jobSucceed:
                    sendDisplayMessageToClient(out, "Job already finished!");
                    break;
                case DefaultKeys.jobCanceled:
                    sendDisplayMessageToClient(out, "Job already canceled!");
                    break;
                default:
                    if(job.secretaryForWorker == null) {
                        Storage.jLock.lock();
                        Storage.jobQueue.remove(job);
                        Storage.jobList.get(job.ID).state = DefaultKeys.jobCanceled;
                        Storage.jLock.unlock();
                        sendDisplayMessageToClient(out, "Job canceled!");
                    }else {
                        job.secretaryForWorker.jLock.lock();
                        job.secretaryForWorker.cancelQueue.offer(job);
                        job.secretaryForWorker.jLock.unlock();
                        sendDisplayMessageToClient(out, "Job canceling... use check method to confirm!");
                    }
            }
        }else {
            sendDisplayMessageToClient(out,"Wrong Job ID!");
        }
    }

    private void receiveJob(String id) {
        Job job = Storage.jobList.get(id);
        if(job != null) {
            switch (job.state) {
                case DefaultKeys.jobSucceed:
                    List<String> filePaths = new ArrayList<String>();
                    filePaths.add(DefaultKeys.workDir + job.ID + "/" + DefaultKeys.outputFileName);
                    filePaths.add(DefaultKeys.workDir + job.ID + "/" + DefaultKeys.billFileName);
                    sendDownloadFilePermitMessageToClient(out, filePaths);
                    break;
                default:
                    sendDisplayMessageToClient(out,"Sorry, job in progress! Please wait patiently.");
            }
        }else {
            sendDisplayMessageToClient(out,"Wrong Job ID!");
        }
    }

    ServerOneClient(Socket s) throws IOException {
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
                switch (firstStr.toUpperCase()) {
                    case DefaultKeys.startOption:
                        if(arrayStr.length >= 5) {
                            try {
                                int rep = Integer.valueOf(arrayStr[4]);
                                System.out.println(rep);
                                for(int i=0; i<rep; i++) {
                                    startANewJob(arrayStr);
                                }
                            }catch (Exception e) {
                                sendDisplayMessageToClient(out, DefaultKeys.wrongOptionMessage);
                            }
                        }else {
                            startANewJob(arrayStr);
                        }
                        break;
                    case DefaultKeys.checkOption:
                        if(arrayStr.length != 2) {
                            sendDisplayMessageToClient(out, "Missing parameters!");
                        }else {
                            checkJobState(arrayStr[1]);
                        }
                        break;
                    case DefaultKeys.cancelOption:
                        if(arrayStr.length != 2) {
                            sendDisplayMessageToClient(out, "Missing parameters!");
                        }else {
                            cancelJob(arrayStr[1]);
                        }
                        break;

                    case DefaultKeys.receiveOption:
                        if(arrayStr.length != 2) {
                            sendDisplayMessageToClient(out, "Missing parameters!");
                        }else {
                            receiveJob(arrayStr[1]);
                        }
                        break;
                    default:
                        sendDisplayMessageToClient(out, DefaultKeys.wrongOptionMessage);
                }
                /*if (firstStr.toUpperCase().equals(DefaultKeys.startOption)) {

                }else if (firstStr.toUpperCase().equals(DefaultKeys.checkOption)) {


                }else if (firstStr.toUpperCase().equals(DefaultKeys.cancleOption)) {
                    sendDisplayMessageToClient(out, "cancle ....asd");

                }else if (firstStr.toUpperCase().equals(DefaultKeys.receiveOption)) {
                    sendDisplayMessageToClient(out, "receive ....asd");
                }else {
                    sendDisplayMessageToClient(out, DefaultKeys.wrongOptionMessage);
                }*/
                sendInquiryMessageToClient(out, DefaultKeys.pressEnterToContinueMessage);
                receiveClientReplyParameter(in);
            }

        }
    }

}