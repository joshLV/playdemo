package util.common;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: tanglq
 * Date: 13-1-24
 * Time: 下午1:53
 */
public class InfoUtil {

    private static Pattern PATTERN_CHARSEQUENCE = Pattern.compile("([[a-zA-Z0-9]]+)");

    /**
     * 得到隐藏处理过的券号
     *
     * @return 券号
     */
    public static String getMaskedEcouponSn(String eCouponSn) {
        StringBuilder sn = new StringBuilder();
        int len = eCouponSn.length();
        if (len > 4) {
            for (int i = 0; i < len - 4; i++) {
                sn.append("*");
            }
            sn.append(eCouponSn.substring(len - 4, len));
        }
        return sn.toString();
    }

    public static String getSpecifiedLengthDescription(int length, String description) {
        if (StringUtils.isNotBlank(description)) {
            return description.length() >= length ? new String(description.substring(0, length)) + "......" : description;
        } else {
            return "";
        }
    }

    public static String getMaskedPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return "";
        }
        StringBuilder sbPhone = new StringBuilder();
        sbPhone.append(phone.substring(0, 3));
        sbPhone.append("****");

        int len = phone.length();
        if (len > 10) {
            sbPhone.append(phone.substring(len - 4, len));
        }
        return sbPhone.toString();
    }

    /**
     * 返回第一段数字和字母的值
     *
     * @param value
     * @return
     */
    public static String getFirstCharSequence(String value) {
        Matcher matcher = PATTERN_CHARSEQUENCE.matcher(value);
        if (matcher.find()) {
            return matcher.group(0);
        }
        // 安全起见，直接返回券号
        return value;
    }
}
