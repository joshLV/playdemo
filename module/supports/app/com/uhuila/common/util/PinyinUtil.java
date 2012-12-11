package com.uhuila.common.util;

import net.sourceforge.pinyin4j.PinyinHelper;

/**
 * 拼音操作工具类<p/>
 * User: yanjy
 * Date: 12-12-10
 * Time: 下午5:22
 */
public class PinyinUtil {

    /**
     * 将字符串数组转换成字符串
     *
     * @param str
     * @param separator 各个字符串之间的分隔符
     * @return
     */
    public static String stringArrayToString(String[] str, String separator) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length; i++) {
            sb.append(str[i]);
            if (str.length != (i + 1)) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    /**
     * 简单的将各个字符数组之间连接起来
     *
     * @param str
     * @return
     */
    public static String stringArrayToString(String[] str) {
        return stringArrayToString(str, "");
    }

    /**
     * 将字符数组转换成字符串
     *
     * @param ch
     * @param separator 各个字符串之间的分隔符
     * @return
     */
    public static String charArrayToString(char[] ch, String separator) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ch.length; i++) {
            sb.append(ch[i]);
            if (ch.length != (i + 1)) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    /**
     * 取汉字的首字母
     *
     * @param src
     * @param isCapital 是否是大写
     * @return
     */
    public static char[] getHeadByChar(char src, boolean isCapital) {
        //如果不是汉字直接返回
        if (src <= 128) {
            return new char[]{src};
        }
        //获取所有的拼音
        String[] pinyingStr = PinyinHelper.toHanyuPinyinStringArray(src);
        //创建返回对象
        int polyphoneSize = pinyingStr.length;
        char[] headChars = new char[polyphoneSize];
        int i = 0;
        //截取首字符
        for (String s : pinyingStr) {
            char headChar = s.charAt(0);
            //首字母是否大写，默认是小写
            if (isCapital) {
                headChars[i] = Character.toUpperCase(headChar);
            } else {
                headChars[i] = headChar;
            }
            i++;
        }

        return headChars;
    }

    /**
     * 查找字符串首字母
     *
     * @param src
     * @return
     */
    public static String[] getHeadByString(String src) {
        return getHeadByString(src, true);
    }

    /**
     * 查找字符串首字母
     *
     * @param src
     * @param isCapital 是否大写
     * @return
     */
    public static String[] getHeadByString(String src, boolean isCapital) {
        return getHeadByString(src, isCapital, null);
    }

    /**
     * 查找字符串首字母
     *
     * @param src
     * @param isCapital 是否大写
     * @param separator 分隔符
     * @return
     */
    public static String[] getHeadByString(String src, boolean isCapital, String separator) {
        char[] chars = src.toCharArray();
        String[] headString = new String[chars.length];
        int i = 0;
        for (char ch : chars) {

            char[] chs = getHeadByChar(ch, isCapital);
            StringBuffer sb = new StringBuffer();
            if (null != separator) {
                int j = 1;

                for (char ch1 : chs) {
                    sb.append(ch1);
                    if (j != chs.length) {
                        sb.append(separator);
                    }
                    j++;
                }
            } else {
                sb.append(chs[0]);
            }
            headString[i] = sb.toString();
            i++;
        }
        return headString;
    }

    public static String getHeadLetterString(String letter) {
        String headStr = "";
        for (int i = 0; i < letter.length(); i++) {
            String[] r = PinyinHelper.toHanyuPinyinStringArray(letter.charAt(i));
            headStr += r[0].charAt(0);
        }
        return headStr;
    }

    public static void main(String[] args) {
        System.out.println(stringArrayToString(getHeadByString("我se 心")));
    }

}