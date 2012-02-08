package com.uhuila.common.util.util;

import java.io.File;

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
    public static String getPathById(int id) {
        int firstDir = id >> 20;
        int secondTmpNum = id >> 10;
        int secondDir = (~(firstDir << 10)) & (secondTmpNum);

        int thirdDir = (~(secondTmpNum << 10)) & id;
        return String.valueOf(firstDir) + "/" + secondDir + "/" + thirdDir + "/";
    }
    
}
