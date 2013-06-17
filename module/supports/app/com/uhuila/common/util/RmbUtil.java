package com.uhuila.common.util;

/**
 * User: tanglq
 * Date: 13-6-17
 * Time: 下午9:35
 */
public class RmbUtil {

    // 大写数字字符串
    public static String[] RMB = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
    // 单位数组
    public static String[] unit = {"角", "分", "元", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿"};

    // 数字字符串
    public static String[] value = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

    // 单位数组
    public static String[] valueUnit = {"角", "分", "元", "拾", "佰", "仟", "万", "拾万", "佰万", "仟万"};

    // 转换成大写RMB
    public static String toRMB(String num) {
        // 结果
        String result = "";

        // 将待转换的数字分解成整数及小数2部分
        String integer = divide(num, true);
        String decimal = divide(num, false);
        // 将整数部分及小数部分分别转换成相应大写，并添加单位
        result += convert(integer, true) + "元";
        result += convert(decimal, false);


        // 正则表达式替换，清除多余的零
        result = zeroClear(result);


        result = result.replaceAll("元元", "元");
        result = result.replaceAll("零", "");
        if (result.contains("角") && !result.contains("分")) {
            result = result.replaceAll("角", "角整");
        } else if (!result.contains("角") && !result.contains("分")) {
            result = result.replaceAll("元", "元整");
        }

        return result;
    }


    /**
     * 把大写中的汉字换成数字，以方便电话验证时TTS的发音
     * @param num
     * @return
     */
    public static String toNumberRMB(String num) {
        // 结果
        String result = toRMB(num);

        for (int i = 0; i < RMB.length; i++) {
            result = result.replaceAll(RMB[i], value[i]);
        }

        return result;
    }

    // 替换结果中的"零分"、"零角"、"零元"、"零拾"、"零佰"、"零仟"、"零万"
    public static String zeroClear(String str) {
        String[] regex = {"零分", "零角", "零拾", "零佰", "零仟"};

        //结果
        String result = str;

        // 正则表达式替换
        for (int i = 0; i < regex.length; i++) {
            result = result.replace(regex[i], "零");
        }

        // 清除多余的零
        result = result.replace("零零零零", "");
        result = result.replace("零零零", "");
        result = result.replace("零零", "");
        result = result.replace("零万", "");
        result = result.replace("零元", "");
        result = result.replace("零分", "");


        return result;
    }

    // 将待转换的数字分解成整数及小数2部分
    public static String divide(String num, boolean isIntegerPart) {
        String result = "";

        // 若isIntegerPart为true，表明截取的是整数部分
        if (isIntegerPart) {
            // 用以"."为分割符，应写成"\\."才符合正则表达式
            result = num.split("\\.")[0];
            // 去除开头多余的"0"
            while (result.charAt(0) == '0') {
                result = result.substring(1);
            }

            return result;
        } else {
            String tmp = num.split("\\.")[1];
            // 截取小数点后2位
            result = tmp.substring(0, 2);
            return result;
        }
    }

    // 将整数部分及小数部分分别转换成相应大写，并添加单位
    public static String convert(String str, boolean isIntegerPart) {
        String result = "";

        int strLength = str.length();
        // 转换整数部分
        if (isIntegerPart) {
            for (int i = 0; i < strLength; i++) {
                // 将字符转换成相应的数字大写
                result += RMB[str.charAt(i) - 48];
                // 添加单位
                result += unit[strLength - i + 1];
            }

            return result;

        } else {
            for (int i = 0; i < strLength; i++) {
                // 将字符转换成相应的数字大写
                result += RMB[str.charAt(i) - 48];
                // 添加单位
                result += unit[i];
            }

            return result;
        }

    }

}
