package com.uhuila.common.util;

import org.apache.commons.codec.digest.DigestUtils;

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
    private static final Pattern IMAGE_PATTERN = Pattern.compile("^/([0-9]+)/([0-9]+)/([0-9]+)/(.+).((?i)(jpg|png|gif|jpeg))$");
    private static final Pattern FILENAME_PATTERN = Pattern.compile("(.*/)*(.*)$");
    public static Pattern PATH_MARKER_PATTERN = Pattern.compile("^(.+)(\\..+)$");
    private static final String HTTP_HEAD = "http://";

    private static final String SIZE_KEY = "sJ34fds29h@d";

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
        firstDir =  firstDir % 1000;

        long thirdDir = (~(secondTmpNum << 10)) & id;
        return "/" + String.valueOf(firstDir) + "/" + secondDir + "/" + thirdDir + "/";
    }

    /**
     * 添加图片标识，如nw则不加水印，WIDTHxHEIGHT指定长宽等
     *
     * @param path 路径
     * @param mark 标识
     * @return 添加了标识的图片路径
     */
    public static String addImgPathMark(String path, String mark){
        Matcher matcher = PATH_MARKER_PATTERN.matcher(path);
        if(!matcher.matches()){
            return null;
        }
        return matcher.group(1) + "_" + mark + matcher.group(2);
    }

    /**
     * 请求加密后的图片路径
     *
     * @param requestUri 文件路径 接受 /a/b/c.abc  /a.bc  a.bc 等不同形式的参数
     * @return 请求签名
     */
    public static String signImgPath(String requestUri){
        Matcher matcher = FILENAME_PATTERN.matcher(requestUri);
        if(!matcher.matches()){
            return null;
        }
        String pre = matcher.group(1) == null ? "" : matcher.group(1);
        String sign = imgSign(matcher.group(2));
        return pre + sign + "_" + matcher.group(2);
    }

    public static String imgSign(String fileName){
        return DigestUtils.md5Hex(fileName + "-" + SIZE_KEY).substring(24);
    }

    /**
     * 根据图片服务器以及图片路径生成图片的url.
     *
     * @param imageServer 图片服务器域名或ip
     * @param imagePath   图片路径
     * @param fix        图片大小规格
     * @return 完整的图片url
     */
    public static String getImageUrl(String imageServer, String imagePath, String fix) {
        if(imageServer == null || imagePath == null){
            return null;
        }

        if(fix == null){
            fix = "";
        }
        String server = imageServer != null && imageServer.startsWith("http://") ? imageServer : HTTP_HEAD + imageServer;
        Matcher matcher = IMAGE_PATTERN.matcher(imagePath);
        if(!matcher.matches()){
            return null;
        }

        String fixName = fix.equals("")  ? "" : "_" + fix;

        String newFileName = matcher.group(4) + fixName + "." + matcher.group(5);

        return server +  IMAGE_ROOT_GENERATED + signImgPath(
                "/" + matcher.group(1)
                + "/" + matcher.group(2)
                + "/" + matcher.group(3)
                + "/" + newFileName
            );
    }
}
