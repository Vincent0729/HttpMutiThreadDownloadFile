package util;


public class LogUtils {
    private static String TAG = "LogUtils";
    public static boolean DEBUG = true;

    //static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

    public static void info(String msg, Object... arg) {
        print(msg, " -INFO- ", arg);
    }

    public static void error(String msg, Object... arg) {
        print(msg, " -ERROR-", arg);
    }

    public static void debug(String msg, Object... arg) {
        if (DEBUG) {
            print(msg, " -DEBUG-", arg);
        }
    }

    private static void print(String msg, String level, Object... arg) {
        if (arg != null && arg.length > 0) {
            msg = String.format(msg.replace("{}", "%s"), arg);
        }
        String thread = Thread.currentThread().getName();
        LogUtils.d(TAG, thread + level + msg);
    }

    public static void d(String TAG,String msg){
        System.out.println(TAG+":"+msg);
    }
}
