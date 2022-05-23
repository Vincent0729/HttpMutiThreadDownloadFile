import thread.DownloadMainThread;
import util.LogUtils;

public class Main {
    private static String TAG = Main.class.getName();

    public static void main(String[] argv) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtils.d(TAG, "start Runnable");
                DownloadMainThread fileDownload = new DownloadMainThread();
                try {
                    fileDownload.download("http://wppkg.baidupcs.com/issue/netdisk/yunguanjia/BaiduYunGuanjia_7.0.1.1.exe");
                    //fileDownload.download("https://riptutorial.com/Download/webrtc.pdf");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
