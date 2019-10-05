import com.jcraft.jsch.*;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    public static void uploadFile(String src, String dst,String host) {
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

            sftpChannel.put(src, dst, ChannelSftp.OVERWRITE);

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

       /* String src6 = "out\\production\\PaaS\\Worker.class"; // 本地文件名
        String dst6 = "/home/ubuntu/PaaS/Worker.class";//"/home/ubuntu/Input/test.txt"; // 目标文件名
        uploadFile(src6,dst6,worker1);*/

        /*String master = "144.6.227.102";
        String src1 = "out\\production\\PaaS\\ServerEnd.class"; // 本地文件名
        String dst1 = "/home/ubuntu/PaaS/ServerEnd.class";//"/home/ubuntu/Input/test.txt"; // 目标文件名
        uploadFile(src1,dst1,master);

        String src2 = "out\\production\\PaaS\\DefaultKeys.class"; // 本地文件名
        String dst2 = "/home/ubuntu/PaaS/DefaultKeys.class";//"/home/ubuntu/Input/test.txt"; // 目标文件名
        uploadFile(src2,dst2,master);

        String src3 = "out\\production\\PaaS\\Job.class"; // 本地文件名
        String dst3 = "/home/ubuntu/PaaS/Job.class";//"/home/ubuntu/Input/test.txt"; // 目标文件名
        uploadFile(src3,dst3,master);

        String src4 = "out\\production\\PaaS\\Storage.class"; // 本地文件名
        String dst4 = "/home/ubuntu/PaaS/Storage.class";//"/home/ubuntu/Input/test.txt"; // 目标文件名
        uploadFile(src4,dst4,master);

        String src5 = "out\\production\\PaaS\\ServerOneClient.class"; // 本地文件名
        String dst5 = "/home/ubuntu/PaaS/ServerOneClient.class";//"/home/ubuntu/Input/test.txt"; // 目标文件名
        uploadFile(src5,dst5,master);*/
        runCommand();
    }
}
