package com.uhuila.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * <p/>
 * User: yanjy
 * Date: 12-5-22
 * Time: 上午10:56
 */
public class FieldCheckUtil {
    static Pattern patternNumber = Pattern.compile("[0-9]*");
    static Pattern pattern = Pattern.compile("\\#\\d+\\#");
    /**
     * 判断是否为数字
     * @param str 字符串
     * @return true or false
     */
    public static boolean isNumeric(String str) {

        Matcher isNum = patternNumber.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }
     /**
     * 判断是否符合券号规则
     * @param str 字符串
     * @return true or false
     */
    public static boolean isNumericRule(String str) {
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }


}

