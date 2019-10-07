import com.jcraft.jsch.*;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class uploadFileTemp {
    public static void runCommand() {
        try {
            String command = "java /home/ubuntu/PaaS/ServerEnd";
            String host = DefaultKeys.masterIP;
            String user = "ubuntu";
            String privateKey = "D:\\University of Tasmania\\2019\\Semester 2\\KIT318 Big Data and Cloud Computing\\key.pem";
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            Properties config = new Properties();
            session.setPassword("KIT318");
            jsch.addIdentity(privateKey);
            System.out.println("identity added ");
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            Channel channel = session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);

            //channel.setInputStream(null);
            //((ChannelExec)channel).setErrStream(System.err);
            channel.setInputStream(System.in);
            channel.connect();

            InputStream input = channel.getInputStream();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(input));
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }

            } catch (IOException io) {
                System.out.println("Exception occurred during reading file from SFTP server due to " + io.getMessage());
                io.getMessage();

            } catch (Exception e) {
                System.out.println("Exception occurred during reading file from SFTP server due to " + e.getMessage());
                e.getMessage();

            }
        }catch(Exception e){
            System.out.println(e);
        }
    }

    public static void uploadFile(List<String> srcs, List<String> dsts, String host) {
        try {
            //host = "115.146.84.200";//"144.6.227.102";
            String user = "ubuntu";
            String privateKey = "D:\\University of Tasmania\\2019\\Semester 2\\KIT318 Big Data and Cloud Computing\\key.pem";
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            Properties config = new Properties();
            //session.setPassword("KIT418@utas"); ////if password is empty please comment it
            jsch.addIdentity(privateKey);
            System.out.println("identity added ");
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;

            int index = 0;
            for(String src:srcs) {
                sftpChannel.put(src, dsts.get(index), ChannelSftp.OVERWRITE);
                index++;
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

    public static void main(String[] args) {


        String worker1 = "144.6.227.83";
        String workerTommy = "115.146.84.200";
        String workerLiujin = "144.6.227.83";
        String src6 = "app.py"; // 本地文件名
        String dst6 = "/home/ubuntu/app.py";//"/home/ubuntu/Input/test.txt"; // 目标文件名


        List<String> MasterSrcs = new ArrayList<String>();
        List<String> MasterDsts = new ArrayList<String>();

        MasterSrcs.add("out\\production\\PaaS\\FileIO.class");
        MasterDsts.add("/home/ubuntu/PaaS/FileIO.class");

        MasterSrcs.add("out\\production\\PaaS\\Cloud.class");
        MasterDsts.add("/home/ubuntu/PaaS/Cloud.class");

        MasterSrcs.add("out\\production\\PaaS\\DefaultKeys.class");
        MasterDsts.add("/home/ubuntu/PaaS/DefaultKeys.class");

        MasterSrcs.add("out\\production\\PaaS\\Storage.class");
        MasterDsts.add("/home/ubuntu/PaaS/Storage.class");

        MasterSrcs.add("out\\production\\PaaS\\WorkerEndAdapter.class");
        MasterDsts.add("/home/ubuntu/PaaS/WorkerEndAdapter.class");

        MasterSrcs.add("out\\production\\PaaS\\Job.class");
        MasterDsts.add("/home/ubuntu/PaaS/Job.class");

        MasterSrcs.add("out\\production\\PaaS\\MasterEnd.class");
        MasterDsts.add("/home/ubuntu/PaaS/MasterEnd.class");

        MasterSrcs.add("out\\production\\PaaS\\ClientAPMonitor.class");
        MasterDsts.add("/home/ubuntu/PaaS/ClientAPMonitor.class");

        MasterSrcs.add("out\\production\\PaaS\\WorkerEndAPMonitor.class");
        MasterDsts.add("/home/ubuntu/PaaS/WorkerEndAPMonitor.class");

        MasterSrcs.add("out\\production\\PaaS\\SecretaryAPMonitor.class");
        MasterDsts.add("/home/ubuntu/PaaS/SecretaryAPMonitor.class");

        MasterSrcs.add("out\\production\\PaaS\\WorkerAPMonitor.class");
        MasterDsts.add("/home/ubuntu/PaaS/WorkerAPMonitor.class");

        MasterSrcs.add("out\\production\\PaaS\\ServerOneClient.class");
        MasterDsts.add("/home/ubuntu/PaaS/ServerOneClient.class");

        MasterSrcs.add("out\\production\\PaaS\\ServerOneSecretary.class");
        MasterDsts.add("/home/ubuntu/PaaS/ServerOneSecretary.class");

        MasterSrcs.add("out\\production\\PaaS\\ServerOneWorker.class");
        MasterDsts.add("/home/ubuntu/PaaS/ServerOneWorker.class");

        MasterSrcs.add("app.py");
        MasterDsts.add("/home/ubuntu/PaaS/app.py");

        uploadFile(MasterSrcs,MasterDsts,DefaultKeys.masterIP);

        List<String> workerSrcs = new ArrayList<String>();
        List<String> workerDsts = new ArrayList<String>();

        workerSrcs.add("out\\production\\PaaS\\FileIO.class");
        workerDsts.add("/home/ubuntu/PaaS/FileIO.class");

        workerSrcs.add("out\\production\\PaaS\\DefaultKeys.class");
        workerDsts.add("/home/ubuntu/PaaS/DefaultKeys.class");

        workerSrcs.add("out\\production\\PaaS\\Storage.class");
        workerDsts.add("/home/ubuntu/PaaS/Storage.class");

        workerSrcs.add("out\\production\\PaaS\\Job.class");
        workerDsts.add("/home/ubuntu/PaaS/Job.class");

        workerSrcs.add("out\\production\\PaaS\\WorkBook.class");
        workerDsts.add("/home/ubuntu/PaaS/WorkBook.class");

        workerSrcs.add("out\\production\\PaaS\\WorkerEnd.class");
        workerDsts.add("/home/ubuntu/PaaS/WorkerEnd.class");

        workerSrcs.add("out\\production\\PaaS\\Worker.class");
        workerDsts.add("/home/ubuntu/PaaS/Worker.class");

        workerSrcs.add("out\\production\\PaaS\\Secretary.class");
        workerDsts.add("/home/ubuntu/PaaS/Secretary.class");

        workerSrcs.add("out\\production\\PaaS\\ServerOneSecretary.class");
        workerDsts.add("/home/ubuntu/PaaS/ServerOneSecretary.class");

        workerSrcs.add("out\\production\\PaaS\\ServerOneWorker.class");
        workerDsts.add("/home/ubuntu/PaaS/ServerOneWorker.class");

        workerSrcs.add("key.pem");
        workerDsts.add("/home/ubuntu/PaaS/key.pem");

        workerSrcs.add("jsch-0.1.55.jar");
        workerDsts.add("/home/ubuntu/PaaS/jsch-0.1.55.jar");

        uploadFile(workerSrcs,workerDsts,DefaultKeys.worker2IP);

        List<String> WorkerSrcs = new ArrayList<String>();
        List<String> WorkerDsts = new ArrayList<String>();

        WorkerSrcs.add("/home/ubuntu/app.py");
        WorkerDsts.add("add.py");

        //FileIO.downloadFile(DefaultKeys.masterIP,"key.pem" ,WorkerSrcs, WorkerDsts);
    }
}
