package com.uhuila.common.util;

import com.uhuila.common.constants.ImageSize;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件路径生成工具.
 * User: sujie
 * Date: 2/7/12
 * Time: 5:04 PM
 */
public class PathUtil {

    private static final String IMAGE_ROOT_GENERATED = "/p";
    private static final String IMAGE_ROOT_ORIGINAL = "/o";
    private static final Pattern IMAGE_PATTERN = Pattern.compile("^/([0-9]+)/([0-9]+)/([0-9]+)/([^_]+).(jpg|png|gif|jpeg)$");
    private static final String HTTP_HEAD = "http://";

    /**
     * 根据图片id生成三级目录的路径.
     *
     * @param id 图片id
     * @return 三级目录的路径
     */
    public static String getPathById(long id) {
        long firstDir = id >> 20;
        long secondTmpNum = id >> 10;
        long secondDir = (~(firstDir << 10)) & (secondTmpNum);

        long thirdDir = (~(secondTmpNum << 10)) & id;
        return "/" + String.valueOf(firstDir) + "/" + secondDir + "/" + thirdDir + "/";
    }

    /**
     * 根据图片服务器以及图片路径生成图片的url.
     *
     * @param imageServer 图片服务器域名或ip
     * @param imagePath   图片路径
     * @param size        图片大小规格
     * @return 完整的图片url
     */
    public static String getImageUrl(String imageServer, String imagePath, ImageSize size) {
        String sizeType;
        String rootPath;
        if (size == null || ImageSize.ORIGINAL.equals(size)) {
            sizeType = "";
            rootPath = IMAGE_ROOT_ORIGINAL;
        } else {
            sizeType = "_" + size.toString();
            rootPath = IMAGE_ROOT_GENERATED;
        }
        String server = imageServer != null && imageServer.startsWith("http://") ? imageServer : HTTP_HEAD + imageServer;
        String defaultImage = server + rootPath + "/1/1/1/default" + sizeType + ".png";
        if (imagePath == null || imagePath.equals("")) {
            return defaultImage;
        }
        Matcher matcher = IMAGE_PATTERN.matcher(imagePath);
        if (!matcher.matches()) {
            return defaultImage;
        }
        if (size == null || ImageSize.ORIGINAL.equals(size)) {
            return server + rootPath + imagePath;
        }

        String imageHeadStr = IMAGE_ROOT_GENERATED + imagePath;
        return server + imageHeadStr.replace("/" + matcher.group(4) + "." + matcher.group(5),
                "/" + matcher.group(4) + sizeType + "." + matcher.group(5));
    }
}
