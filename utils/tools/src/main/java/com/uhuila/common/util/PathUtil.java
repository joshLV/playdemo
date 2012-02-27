package com.uhuila.common.util;

/**
 * 文件路径生成工具.
 * User: sujie
 * Date: 2/7/12
 * Time: 5:04 PM
 */
public class PathUtil {

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

}
