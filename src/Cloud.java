import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.image.Image;
import org.openstack4j.openstack.OSFactory;

public class Cloud {
    OSClientV3 masterOS=null;
    OSClientV3 worker1OS=null;
    OSClientV3 worker2OS=null;
    OSClientV3 standbyWorkerOS=null;

    List servers;

    public Cloud() {
        masterOS = OSFactory.builderV3()
                .endpoint("https://keystone.rc.nectar.org.au:5000/v3")
                .credentials("yinzix@utas.edu.au", "NDAxMjQxNzMyODAwNWI2",Identifier.byName("Default"))
                .scopeToProject(Identifier.byId("72a0388a8ea44bdeb096e05edc6974f8"))
                .authenticate();

       /* worker1OS = OSFactory.builderV3()
                .endpoint("https://keystone.rc.nectar.org.au:5000/v3")
                .credentials("yypeng@utas.edu.au", "Y2ZmYzE5Y2JjMWMwZjZk",Identifier.byName("Default"))
                .scopeToProject(Identifier.byId("f4c9952b6d7843bcbc6c183a6617b150"))
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

    public void createServer() {
        ServerCreate server = Builders.server()
                .name("KIT318")
                .flavor("1")
                .image("99d9449a-084f-4901-8bd8-c04aebd589ca")
                .keypairName("key")
                .build();

        standbyWorkerOS.compute().servers().boot(server);
    }

    //List of all flavors
    public void ListFlavors(OSClientV3 os) {

        List<Flavor> flavors = (List<Flavor>) os.compute().flavors().list();
        System.out.println(flavors);
    }

    //List of all images
    public void ListImages(OSClientV3 os) {

        List<? extends Image> images = (List<? extends Image>) os.compute().images().list();
        System.out.println(images);
    }
    //List of all Servers
    public void ListServers(OSClientV3 os) {

        List<? extends Server> servers = os.compute().servers().list();
        System.out.println(servers);
    }
    //Delete a Server
    public void deleteServer(OSClientV3 os) {
        os.compute().servers().delete("2cecd39a-97fd-417f-9eda-38185f0d4918");
    }
}
