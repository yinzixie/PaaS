import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.api.exceptions.ClientResponseException;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.image.Image;
import org.openstack4j.openstack.OSFactory;

public class Cloud {
    OSClientV3 os=null;
    public boolean result = false;
    String newIP = null;

    int waitsec = 20 * 1000; //30s
    String acc = "jliu40@utas.edu.au";
    String pwd = "ZTJkZGEwYTNmZWE4NDFi";

    /*OSClientV3 masterOS=null;
    OSClientV3 worker1OS=null;
    OSClientV3 worker2OS=null;
    OSClientV3 standbyWorkerOS=null;*/

    List servers;

    public Cloud() {
        os = OSFactory.builderV3()
                .endpoint("https://keystone.rc.nectar.org.au:5000/v3")
                .credentials("yuhangy@utas.edu.au", "Y2QyMGRiNzVkMjllOTQ3", Identifier.byName("Default"))
                .scopeToProject(Identifier.byId("9febfc289d494d55a7509c084e446a5e"))
                .authenticate();
        /*os = OSFactory.builderV3()
                .endpoint("https://keystone.rc.nectar.org.au:5000/v3")
                .credentials("yypeng@utas.edu.au", "YzNlMDYyNThmNWQ4MmNi", Identifier.byName("Default"))
                .scopeToProject(Identifier.byId("f4c9952b6d7843bcbc6c183a6617b150"))
                .authenticate();*/

       /* masterOS = OSFactory.builderV3()
                .endpoint("https://keystone.rc.nectar.org.au:5000/v3")
                .credentials("yinzix@utas.edu.au", "NDAxMjQxNzMyODAwNWI2",Identifier.byName("Default"))
                .scopeToProject(Identifier.byId("72a0388a8ea44bdeb096e05edc6974f8"))
                .authenticate();

        worker2OS = OSFactory.builderV3()
                .endpoint("https://keystone.rc.nectar.org.au:5000/v3")
                .credentials("jliu@utas.edu.au", "Y2U3ZWY4ZDRiYzlhNjkw",Identifier.byName("Default"))
                .scopeToProject(Identifier.byId("7390768c1e5f44ddbc81961068ca91bf"))
                .authenticate();

        standbyWorkerOS = OSFactory.builderV3()
                .endpoint("https://keystone.rc.nectar.org.au:5000/v3")
                .credentials("yuhangy@utas.edu.au", "ZTQ1NjdhMDgxOWIwMTc3",Identifier.byName("Default"))
                .scopeToProject(Identifier.byId("9febfc289d494d55a7509c084e446a5e"))
                .authenticate();*/


        //servers = masterOS.compute().servers().list();
        //servers.addAll(standbyWorkerOS.compute().servers().list());

        //System.out.println(servers);
    }

    public static class newWorker{
        static String name = "newWorker";
        static String flavor = "cba9ea52-8e90-468b-b8c2-777a94d81ed3";
        static String image = "9b0615c9-5fe2-4410-ad2a-a5306778496c";
        static String ip4 = null;
        static String status = Server.Status.UNKNOWN.name();
        static int powerState = 0;
    }

    @SuppressWarnings("finally")
    public String CreateServer() {
        System.out.println("Create new instance...");
        try {
            ServerCreate server = Builders.server()
                    .name(newWorker.name)
                    .flavor(newWorker.flavor)
                    .image(newWorker.image)
                    .keypairName("key")
                    .addSecurityGroup("ssh")
                    .build();

            os.compute().servers().boot(server);

            //ListServers(false,worker.name);et ip: 144.6.227.147

            result = true;

        } catch(ClientResponseException e) {
            //e.printStackTrace();
            result = false;
            System.out.println("The instance has already existed " + e.getMessage() + "; getStatus: " + e.getStatus() + "; getStatusCode: "+ e.getStatusCode());

            //CoverServer();

        } catch(Exception e) {
            //e.printStackTrace();
            result = false;
            System.out.println("UNEXPECTED ERROR ... " + e.getMessage());

        }
        finally {
            if(result)
            {
                int try_time = 0;
                while(newWorker.status != "ACTIVE" || newWorker.powerState != 1 || newWorker.ip4 == null) {
                    try {
                        try_time++;
                        Thread.sleep(waitsec);
                        System.out.println("Trying to get new instance ip...");
                        ListServers(false,newWorker.name);
                        if(try_time > 20) {
                            break;
                        }
                    } catch (InterruptedException e) {
                        System.out.println("UNEXPECTED ERROR ... " + e.getMessage());
                    }
                }
            }else {
                System.out.println("Failed to get new instance ip");
                return null;
            }
            System.out.println("Get ip: " + newWorker.ip4);
            return newWorker.ip4;
        }
    }

    //List of all Servers
    public void ListServers(boolean displayAll, String name) {
        List<? extends Server> servers = os.compute().servers().list();
        if(servers!=null && servers.size()>0) {
            if(displayAll) {
                System.out.println(servers);
            }
            else {
                //foreach (Server s in servers)
                for (int i = 0; i<servers.size(); i++){
                    if(servers.get(i).getName().equals(name)) {
                        newWorker.ip4 = servers.get(i).getAccessIPv4();
                        newWorker.status = servers.get(i).getStatus().name();
                        newWorker.powerState = Integer.parseInt( servers.get(i).getPowerState());
                        System.out.println(name + "; " + newWorker.ip4 + "; " + newWorker.status);
                    }
                }
            }
        }
    }

    //Delete a Server
    public void DeleteServer(String identifier) {
        os.compute().servers().delete(identifier);
    }

    //Delete the last server and create a new one
    public void CoverServer() {
        List<? extends Server> servers = os.compute().servers().list();
        if(servers!=null && servers.size()>0) {
            os.compute().servers().delete(servers.get(0).getName());
            //check if it is deleted or not. if deleted, then create a new one
            //....
            CreateServer();
        }
    }

    //List of all flavors
    public void ListFlavors() {
        List<Flavor> flavors = (List<Flavor>) os.compute().flavors().list();
        System.out.println(flavors);
        System.out.println(flavors.get(0).toString());
    }

    //List of all images
    public void ListImages() {
        List<? extends Image> images = (List<? extends Image>) os.compute().images().list();
        System.out.println(images);
    }

    /*public static void main(String[] args) {
        Cloud PaaS = new Cloud();
        PaaS.ListServers(true, null);

        String ip = PaaS.CreateServer();
        if(ip != null) {
            System.out.println("Trying to start Worker End...");
            try {
                TimeUnit.SECONDS.sleep(60);
                //System.out.println(Storage.jobQueue);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            new runCommand(ip, DefaultKeys.privateKey,"java -jar /home/ubuntu/PaaS/WorkerEnd.jar");
        }else {
            System.out.println("");
        }
    }*/
}
