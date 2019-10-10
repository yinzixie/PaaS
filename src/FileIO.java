import com.jcraft.jsch.*;
import java.io.*;
import java.util.List;
import java.util.Properties;

public class FileIO {
    public static boolean downloadFile(String host,String privateKey, List<String> remotePaths, List<String> savePaths) {
        try {
            String user = "ubuntu";
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            Properties config = new Properties();
            //session.setPassword("KIT418@utas"); ////if password is empty please comment it
            jsch.addIdentity(privateKey);
            System.out.println("identity added ");
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            ;
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;

            int index = 0;
            for (String remotePath : remotePaths) {
                File saveF= new File(savePaths.get(index));
                if(saveF.exists()) {
                    saveF.delete();
                }
                System.out.println("Downloading file: " + remotePath);
                BufferedWriter writer = new BufferedWriter(new FileWriter(savePaths.get(index), true)); //local path to store downloaded file
                InputStream stream = sftpChannel.get(remotePath); //server file path
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                    String line;
                    while ((line = br.readLine()) != null) {
                        writer.append(line + "\n");
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
                index++;
            }
            sftpChannel.exit();
            session.disconnect();
            return true;
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    public static boolean uploadFile(String host, String privateKey, List<String> srcs, List<String> dsts) {
        try {
            //host = "115.146.84.200";//"144.6.227.102";
            String user = "ubuntu";
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
                System.out.println("Uploading file: " + src);
                sftpChannel.put(src, dsts.get(index), ChannelSftp.OVERWRITE);
                System.out.println("File Uploaded");
                index++;
            }
            sftpChannel.exit();
            session.disconnect();
            return true;
        } catch (JSchException e) {
            System.out.println("Failed Uploaded File/nError Details: ");
            e.printStackTrace();
        } catch (SftpException e) {
            System.out.println("Failed Uploaded File/nError Details: ");
            e.printStackTrace();
        }
        catch(Exception e){
            System.out.println("Failed Uploaded File/nError Details: ");
            System.out.println(e);
        }
        return false;
    }
}
