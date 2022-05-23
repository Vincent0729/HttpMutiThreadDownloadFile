package util;

import java.io.File;

public class FileUtils {
    /**
     * get file length
     *
     * @param name
     * @return
     */
    public static long getFileContentLength(String name) {
        File file = new File(name);
        return file.exists() && file.isFile() ? file.length() : 0;
    }
}
