public class Job {
    public String ID;
    public String time;

    public String StartTime;
    public String FinishTime;

    public String appType;
    public String appFile;

    public String inputFile;

    public String state;

    public ServerOneSecretary secretaryForWorker;

    Job(String id, String app, String input, String t) {
        ID = id;

        StartTime = DefaultKeys.getTime();

        appFile = app;
        appType = appFile.substring(appFile.lastIndexOf(".") + 1);

        inputFile = input;
        time = t;

        state = "Initializing";
    }
}

