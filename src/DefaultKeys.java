public class DefaultKeys {
    public static final String DISPLAY_MESSAGE_FLAG = "DISPLAY_MESSAGE_START"; //message need to be displayed on the client
    public static final String PARAMETER_MESSAGE_FLAG = "PARAMETER_MESSAGE_START"; //message include parameter need let client know
    public static final String INQUIRY_MESSAGE_FLAG = "INQUIRY_MESSAGE_START"; //message need to be replied from client
    public static final String MESSAGE_END_FLAG = "MESSAGE_END";

    public static final String UPLOAD_FILE_FLAG = "UPLOAD_FILE_PERMIT"; //message tell client can upload files

    public static final String START_UPLOAD_FILE = "START_UPLOAD_FILE";
    public static final String END_UPLOAD_FILE_SUCCEED = "END_UPLOAD_FILE_SUCCEED";
    public static final String END_UPLOAD_FILE_FAILED = "END_UPLOAD_FILE_FAILED";

    public static final String startOption = "START";
    public static final String checkOption = "CHECK";
    public static final String cancleOption = "CANCLE";
    public static final String receiveOption = "RECEIVE";

    public static final String PAUSE_GAME_FLAG = "PAUSE";
    public static final String CLOSE_CLIENT_FLAG = "EXIT";

    public static final String DEFAULT_INQUIRY_MESSAGE = "Send to Server: ";

    public static final String initialMessage = "-----------------------------------\n" +
                                                 "Welcome to PaaS Cloud Platform\n" +
                                                 "Type any of the following options:\n" +
                                                 "Start a new job [start filetype filename remoteLocation time]\n" +
                                                 "Check status of jobs [check passcode]\n" +
                                                 "Cancel job [cancle passcode]\n" +
                                                 "Receive job [receive passcode]\n" +
                                                 "Exit [exit]\n" +
                                                 "-----------------------------------";
    public static final String wrongOptionMessage = "Wrong option! Please check your input!";

}
