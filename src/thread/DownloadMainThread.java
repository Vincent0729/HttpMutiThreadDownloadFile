package thread;

import util.FileUtils;
import util.HttpUtls;
import util.LogUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DownloadMainThread {
    private String TAG = DownloadMainThread.class.getName();
    private ArrayList<Boolean> downloadResultArraylist = new ArrayList<>();
    public static int DOWNLOAD_THREAD_NUM = 5;
    // download thread pool
    private static ExecutorService executor = Executors.newFixedThreadPool(DOWNLOAD_THREAD_NUM + 1);
    // download tmp file
    public static String FILE_TEMP_SUFFIX = ".temp";
    public static String DOWNLOAD_PATH = "/Users/vincent/IdeaProjects/HttpMutiThreadDownloadFile/src";

    public void download(String url) throws Exception {
        String fileName = HttpUtls.getHttpFileName(url);
        fileName = DOWNLOAD_PATH + File.separator + fileName;
        long localFileSize = FileUtils.getFileContentLength(fileName);
        // get Remote Internet file size
        long httpFileContentLength = HttpUtls.getHttpFileContentLength(url);
        LogUtils.d(TAG, "httpFileContentLength=" + httpFileContentLength);
        if (localFileSize >= httpFileContentLength) {
            LogUtils.info("{}alread download finish, no need to download", fileName);
            return;
        }
        List<Future<Boolean>> futureList = new ArrayList<>();
        if (localFileSize > 0) {
            LogUtils.info("start split download {}", fileName);
        } else {
            LogUtils.info("start download document {}", fileName);
        }
        LogUtils.info("start download time {}");
        long startTime = System.currentTimeMillis();
        // split download tsak
        splitDownload(url, futureList);
        LogThread logThread = new LogThread(httpFileContentLength);
        Future<Boolean> future = executor.submit(logThread);
        futureList.add(future);
        // start download
        for (Future<Boolean> booleanFuture : futureList) {
            //get()
            LogUtils.d(TAG, "booleanFuture call get()");
            boolean result = booleanFuture.get();
            downloadResultArraylist.add(result);
            LogUtils.d(TAG, "booleanFuture result=" + result);
        }
        LogUtils.info("document finish download {}，spend time：{}", fileName, (System.currentTimeMillis() - startTime) / 1000 + "s");
        LogUtils.info("finish download time {}");

        for (Boolean b : downloadResultArraylist) {
            if (b == false) {
                LogUtils.info("this document download not complete");
                return;
            }
        }
        // merge file
        boolean merge = merge(fileName);
        if (merge) {
            // clean tmp files
            clearTemp(fileName);
        }
        LogUtils.info("this document download finish~~~");
        return;
    }

    /**
     * split task to multi thread
     *
     * @param url
     * @param futureList
     * @throws IOException
     */
    public void splitDownload(String url, List<Future<Boolean>> futureList) throws IOException {
        long httpFileContentLength = HttpUtls.getHttpFileContentLength(url);
        // task split
        long size = httpFileContentLength / DOWNLOAD_THREAD_NUM;
        long lastSize = httpFileContentLength - (httpFileContentLength / DOWNLOAD_THREAD_NUM * (DOWNLOAD_THREAD_NUM - 1));
        for (int i = 0; i < DOWNLOAD_THREAD_NUM; i++) {
            long start = i * size;
            Long downloadWindow = (i == DOWNLOAD_THREAD_NUM - 1) ? lastSize : size;
            Long end = start + downloadWindow;
            if (start != 0) {
                start++;
            }
            DownloadCallable downloadThread = new DownloadCallable(url, start, end, i, httpFileContentLength);
            Future<Boolean> future = executor.submit(downloadThread);
            futureList.add(future);
        }
    }

    public boolean merge(String fileName) throws IOException {
        LogUtils.info("merge document {}", fileName);
        byte[] buffer = new byte[1024 * 10];
        int len = -1;
        try (RandomAccessFile oSavedFile = new RandomAccessFile(fileName, "rw")) {
            for (int i = 0; i < DOWNLOAD_THREAD_NUM; i++) {
                try (BufferedInputStream bis = new BufferedInputStream(
                        new FileInputStream(fileName + FILE_TEMP_SUFFIX + i))) {
                    while ((len = bis.read(buffer)) != -1) {
                        oSavedFile.write(buffer, 0, len);
                    }
                }
            }
            LogUtils.info("merge document finish {}", fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean clearTemp(String fileName) {
        LogUtils.info("clean tmp file {}{}0-{}", fileName, FILE_TEMP_SUFFIX, (DOWNLOAD_THREAD_NUM - 1));
        for (int i = 0; i < DOWNLOAD_THREAD_NUM; i++) {
            File file = new File(fileName + FILE_TEMP_SUFFIX + i);
            file.delete();
        }
        LogUtils.info("clean done {}{}0-{}", fileName, FILE_TEMP_SUFFIX, (DOWNLOAD_THREAD_NUM - 1));
        return true;
    }
}
