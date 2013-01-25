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
    private static final String EXT_IMAGE_ROOT = "p";

    /**
     * 上传图片
     *
     * @param imageFile   图片文件
     * @param uniqueId    用于生成目录的唯一标识
     * @param needNewName 是否需要重新给图片文件取名
     * @param rootPath    指定的图片存储空间的根目录
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
        String targetFileName = needNewName ? generateUniqueId() + extension : imageFile.getName();

        String targetFilePath = storePath + targetFileName;
        if (new File(targetFilePath).exists()) {
            deleteExtImages(storePath, EXT_IMAGE_ROOT);
            new File(targetFilePath).delete();
        }

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

    public static String deleteExtImages(String storePath, String extImagePath) {
        if (storePath == null || "".equals(storePath)) {
            return null;
        }
        String rootPath = storePath.endsWith(File.separator) ? storePath.substring(0, storePath.length() - 1) : storePath;
        rootPath = storePath.startsWith(File.separator) ? rootPath.substring(1) : rootPath;
        String[] rootDirs = rootPath.split(File.separator);
        String extPath = extImagePath.startsWith(File.separator) ? extImagePath.substring(1) : extImagePath;
        if (rootDirs.length >= 3) {
            rootDirs[rootDirs.length - 4] = extPath;
        }
        String path = "";
        for (String dir : rootDirs) {
            path += File.separator + dir;
        }
        File directory = new File(path);
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                file.delete();
            }
        }
        return path;
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
