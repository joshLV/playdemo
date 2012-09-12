package com.uhuila.common.util;

import java.util.Random;

public class RandomNumberUtil {
    private static String chars = "0123456789";
    private static String chars_letter = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ0123456789";

    /**
     * 产生指定位数的随机数字
     *
     * @param length 长度
     * @return 随机数
     */
    public static String generateSerialNumber(int length) {
        char[] charsArray = chars.toCharArray();
        Random random = new Random(System.currentTimeMillis());
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(charsArray[random.nextInt(charsArray.length)]);
        }
        String text = sb.toString();
        return text;
    }

    /**
     * 产生指定位数的随机数(包含字母)
     *
     * @param length 长度
     * @return 随机数
     */
    public static String generateRandomNumber(int length) {
        char[] charsArray = chars_letter.toCharArray();
        Random random = new Random(System.currentTimeMillis());
        StringBuffer sb = new StringBuffer(length);
        for (int i = 0; i < length; i++) {
            sb.append(charsArray[random.nextInt(charsArray.length)]);
        }
        String text = sb.toString();
        return text;
    }
}
