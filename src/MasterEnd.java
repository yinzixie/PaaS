public class MasterEnd {




    public static void main(String[] args) {
        Cloud PaaS = new Cloud();
        //openstack.createServer();
        //PaaS.ListServers(PaaS.masterOS);
        //openstack.deleteServer();
        //openstack.ListFlavors();
        //openstack.ListImages();

        try {
            WorkerAPMonitor wAPM = new WorkerAPMonitor();
        } catch (Exception e) {
            System.out.println("Failed to start Worker AP Monitor");
            e.printStackTrace();
            System.exit(0);
        }

        try {
            ClientAPMonitor cAPM = new ClientAPMonitor();
        } catch (Exception e) {
            System.out.println("Failed to start Client AP Monitor");
            e.printStackTrace();
            System.exit(0);
        }



        while(true) {

        }

    }
}
