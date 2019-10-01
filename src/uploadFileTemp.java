import com.jcraft.jsch.*;

import java.awt.*;
import java.util.Properties;

public class uploadFileTemp {
    public static void uploadFile(String src, String dst) {
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
        String src1 = "out\\production\\PaaS\\ServerEnd.class"; // 本地文件名
        String dst1 = "/home/ubuntu/PaaS/ServerEnd.class";//"/home/ubuntu/Input/test.txt"; // 目标文件名
        uploadFile(src1,dst1);

        String src2 = "out\\production\\PaaS\\DefaultKeys.class"; // 本地文件名
        String dst2 = "/home/ubuntu/PaaS/DefaultKeys.class";//"/home/ubuntu/Input/test.txt"; // 目标文件名*/
        uploadFile(src2,dst2);

        /*String src3 = "out\\production\\PaaS\\Job.class"; // 本地文件名
        String dst3 = "/home/ubuntu/PaaS/Job.class";//"/home/ubuntu/Input/test.txt"; // 目标文件名
        uploadFile(src3,dst3);

        String src4 = "out\\production\\PaaS\\Storage.class"; // 本地文件名
        String dst4 = "/home/ubuntu/PaaS/Storage.class";//"/home/ubuntu/Input/test.txt"; // 目标文件名
        uploadFile(src4,dst4);*/

        String src5 = "out\\production\\PaaS\\ServerOneClient.class"; // 本地文件名
        String dst5 = "/home/ubuntu/PaaS/ServerOneClient.class";//"/home/ubuntu/Input/test.txt"; // 目标文件名
        uploadFile(src5,dst5);
    }
}
