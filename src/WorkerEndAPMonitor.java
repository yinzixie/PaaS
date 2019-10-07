public class WorkerEndAPMonitor {
    WorkerEndAPMonitor() throws Exception {
        //Start Worker AP Monitor
        WorkerAPMonitor workerAPMonitor = new WorkerAPMonitor();
        //Start Secretary AP Monitor
        SecretaryAPMonitor secretaryAPMonitor = new SecretaryAPMonitor();
    }
}
