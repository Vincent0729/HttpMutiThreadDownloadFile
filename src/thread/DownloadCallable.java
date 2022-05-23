package thread;

import util.FileUtils;
import util.HttpUtls;
import util.LogUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;

public class DownloadCallable implements Callable<Boolean> {
    private String TAG = "DownloadThread";
    /**
     * block size
     */
    private static int BYTE_SIZE = 1024 * 100;
    /**
     * download url
     */
    private String url;
    /**
     * start position
     */
    private long startPos;
    /**
     * end position
     */
    private Long endPos;
    /**
     * thread part
     */
    private Integer part;
    /**
     * content size
     */
    private Long contentLenth;

    public DownloadCallable(String url, long startPos, Long endPos, Integer part, Long contentLenth) {
        this.url = url;
        this.startPos = startPos;
        this.endPos = endPos;
        this.part = part;
        this.contentLenth = contentLenth;
    }

    @Override
    public Boolean call() throws Exception {
        LogUtils.d(TAG,"be called");
        if (url == null || url.trim() == "") {
            throw new RuntimeException("download path error");
        }

        // File name
        String httpFileName = HttpUtls.getHttpFileName(url);
        if (part != null) {
            httpFileName = httpFileName + DownloadMainThread.FILE_TEMP_SUFFIX + part;
        }

        // get Local file size
        String fileName = DownloadMainThread.DOWNLOAD_PATH+ File.separator +httpFileName;
        Long localFileContentLength = FileUtils.getFileContentLength(fileName);
        LogUtils.d(TAG,"localFileContentLength="+localFileContentLength);
        LogThread.LOCAL_FINISH_SIZE.addAndGet(localFileContentLength);
        if (localFileContentLength >= endPos - startPos) {
            LogUtils.info("{} alread download finish, no need to download", fileName);
            LogThread.DOWNLOAD_FINISH_THREAD.addAndGet(1);
            return true;
        }
        if (endPos.equals(contentLenth)) {
            endPos = null;
        }

        HttpURLConnection httpUrlConnection = HttpUtls.getHttpUrlConnection(url, startPos + localFileContentLength, endPos);
        LogUtils.d(TAG,"httpFileName="+fileName);
        // 獲得输入流
        try (InputStream input = httpUrlConnection.getInputStream();
             BufferedInputStream bis = new BufferedInputStream(input);
             RandomAccessFile oSavedFile = new RandomAccessFile(fileName, "rw")) {
            oSavedFile.seek(localFileContentLength);
            byte[] buffer = new byte[BYTE_SIZE];
            int len = -1;
            // 讀到文件末尾则返回-1
            while ((len = bis.read(buffer)) != -1) {
                LogUtils.d(TAG,"fileName="+fileName+",len="+len);
                oSavedFile.write(buffer, 0, len);
                LogThread.DOWNLOAD_SIZE.addAndGet(len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LogUtils.error("ERROR! download file not exist {} ", url);
            return false;
        } catch (Exception e) {
            LogUtils.error("download exception");
            e.printStackTrace();
            return false;
        } finally {
            httpUrlConnection.disconnect();
            LogThread.DOWNLOAD_FINISH_THREAD.addAndGet(1);
        }
        return true;
    }

}
