package com.uhuila.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * 文件上传工具.
 * <p/>
 * User: sujie
 * Date: 3/13/12
 * Time: 10:56 AM
 */
public class FileUploadUtil {

    /**
     * 上传图片
     *
     * @param imageFile   图片文件
     * @param uniqueId    用于生成目录的唯一标识
     * @param needNewName 是否需要重新给图片文件取名
     * @param rootPath    指定的图片存储空间的根目录
     *
     * @return 返回文件的路径
     */
    public static String storeImage(File imageFile, long uniqueId, boolean needNewName,
                                    String rootPath) throws IOException {
        //取得文件
        FileInputStream in = new FileInputStream(imageFile);
        // 将该数据流写入到指定文件中
        String storePath = rootPath + PathUtil.getPathById(uniqueId);

        File targetPath = new File(storePath);
        if (!(targetPath.isDirectory())) {
            targetPath.mkdirs();
        }
        String extension = imageFile.getName().substring(imageFile.getName().lastIndexOf("."), imageFile.getName().length());
        String targetFileName = needNewName ? uniqueId + extension : imageFile.getName();

        String targetFilePath = storePath + targetFileName;

        FileOutputStream out = new FileOutputStream(targetFilePath);
        byte[] buffer = new byte[1024];
        int bytes_read;
        while ((bytes_read = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytes_read);
        }
        in.close();
        out.close();
        return targetFilePath;
    }

    /**
     * 上传图片到指定的图片存储空间下
     *
     * @param imageFile 图片文件
     * @param uniqueId  用于生成目录的唯一标识
     * @param rootPath  指定的图片存储空间的根目录
     * @return 返回文件的路径
     */
    public static String storeImage(File imageFile, long uniqueId, String rootPath) throws IOException {
        return storeImage(imageFile, uniqueId, true, rootPath);
    }


    /**
     * 上传图片到指定的图片存储空间下
     *
     * @param imageFile 图片文件
     * @param rootPath  指定的图片存储空间的根目录
     * @return 返回文件的路径
     */
    public static String storeImage(File imageFile, String rootPath) throws IOException {
        return storeImage(imageFile, generateUniqueId(), true, rootPath);
    }


    /**
     * 生成唯一编号.
     *
     * @return 唯一编号
     */
    public static long generateUniqueId() {
        int random = new Random().nextInt() % 100;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssSSS");
        String now = sdf.format(new Date());
        return Long.parseLong(now + Math.abs(random));
    }
}
