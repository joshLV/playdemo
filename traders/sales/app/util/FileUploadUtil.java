package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUploadUtil {

    /**
     * 上传图片
     *
     * @param file
     * @param storePath
     * @param targetFileName
     */
    public static void storeImage(File file, String storePath, String targetFileName) throws IOException {
        //取得文件名
        FileInputStream in;
        in = new FileInputStream(file);
        // 将该数据流写入到指定文件中
        FileOutputStream out;

        if (!(new File(storePath).isDirectory())) {
            new File(storePath).mkdirs();
        }
        out = new FileOutputStream(storePath + targetFileName);
        byte[] buffer = new byte[1024];
        int bytes_read;
        while ((bytes_read = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytes_read);
        }
        in.close();
        out.close();
    }
}
