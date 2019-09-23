import java.util.List;

import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.image.Image;
import org.openstack4j.openstack.OSFactory;

/**
 * OpenStack Example
 *
 */
public class ServerEnd
{
    OSClientV3 os=null;
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
    }


    public static void main( String[] args )
    {
        ServerEnd openstack=new ServerEnd();
        //openstack.createServer();
        openstack.ListServers();
        openstack.ListFlavors();
        openstack.ListImages();
    }
}
