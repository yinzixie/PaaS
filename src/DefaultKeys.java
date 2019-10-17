import java.text.SimpleDateFormat;
import java.util.Date;

public class DefaultKeys {
    public static final String DISPLAY_MESSAGE_FLAG = "DISPLAY_MESSAGE_START"; //message need to be displayed on the client
    public static final String PARAMETER_MESSAGE_FLAG = "PARAMETER_MESSAGE_START"; //message include parameter need let client know
    public static final String INQUIRY_MESSAGE_FLAG = "INQUIRY_MESSAGE_START"; //message need to be replied from client

    public static final String PERMIT_UPLOAD_FLAG = "PERMIT_UPLOAD_FLAG"; //message tell receiver can upload files
    public static final String PERMIT_DOWNLOAD_FLAG = "PERMIT_DOWNLOAD_FLAG";

    public static final String END_UPLOAD_FILE_SUCCEED = "END_UPLOAD_FILE_SUCCEED";
    public static final String END_UPLOAD_FILE_FAILED = "END_UPLOAD_FILE_FAILED";

    public static final String ASSIGN_JOB_MESSAGE_FLAG = "ASSIGN_JOB_MESSAGE_START";

    public static final String GET_JOB_STATE_FLAG = "GET_JOB_STATE_FLAG";
    public static final String GET_WORKER_STATE_FLAG = "GET_WORKER_STATE_FLAG";

    public static final String JOB_STATE_MESSAGE_FLAG = "JOB_STATE_MESSAGE_START";
    public static final String WORKER_STATE_MESSAGE_FLAG = "WORKER_STATE_MESSAGE_START";

    public static final String MESSAGE_END_FLAG = "MESSAGE_END";

    public static final String CANCEL_JOB_FLAG = "CANCel_JOB";

    public static final String workerDied = "WORKER_DIED";

    public static final String jobSucceed = "Job Succeed";
    public static final String jobCanceled = "Job Canceled";
    public static final String jobFailed= "Job Failed";

    public static final String startOption = "START";
    public static final String checkOption = "CHECK";
    public static final String cancelOption = "CANCEL";
    public static final String receiveOption = "RECEIVE";

    public static final String CLOSE_CLIENT_FLAG = "EXIT";

    public static final String DEFAULT_INQUIRY_MESSAGE = "Send to Server: ";

    public static final String initialMessage = "-----------------------------------\n" +
                                                 "Welcome to PaaS Cloud Platform\n" +
                                                 "Type any of the following options:\n" +

                                                 "Start a new job [start appPath inputFilePath time]\n" +
                                                 "Check status of jobs [check passcode]\n" +
                                                 "Cancel job [cancel passcode]\n" +
                                                 "Receive job [receive passcode]\n" +
                                                 "Exit [exit]\n" +
                                                 "-----------------------------------";
    public static final String pressEnterToContinueMessage = "Press enter to continue...";
    public static final String wrongOptionMessage = "Wrong option! Please check your input!";

    public static final Integer clientPort = 8081; //for client
    public static final Integer workerPort = 8082; //for worker
    public static final Integer secretaryPort = 8083; //for worker's secretary

    public static final Integer workerBusyThreshold = 2;

    public static final String workDir = "/home/ubuntu/PaaS/WorkDir/";

    public static final String privateKey = "/home/ubuntu/PaaS/key.pem"; //"key.pem";//

    public static final String masterIP = "144.6.227.102";//"localhost";//

    public static final String worker0IP = "115.146.86.107";
    public static final String worker2IP = "144.6.227.90";//tommy
    public static final String worker3IP = "115.146.86.167";

    public static final String outputFileName = "output.txt";
    public static final String billFileName = "bill.txt";

    public static String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss a");// a为am/pm的标记
        Date date = new Date();// 获取当前时间
        return sdf.format(date); // 输出已经格式化的现在时间（24小时制）
    }

}
