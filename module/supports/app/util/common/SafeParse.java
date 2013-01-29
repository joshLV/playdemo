package util.common;

import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 安全的转化各种数据值.
 * User: tanglq
 * Date: 13-1-21
 * Time: 下午3:27
 */
public class SafeParse {

    /**
     * 安全转化为BigDecimal.
     * @param value 如果为空则返回null，否则返回具体值.
     * @return
     */
    public static BigDecimal toBigDecimal(final String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return new BigDecimal(value.trim());
    }

    /**
     * 安全转化为Long.
     * @param value 如果为空则返回null，否则返回具体值.
     * @return
     */
    public static Long toLong(final String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return Long.parseLong(value.trim());
    }

    /**
     * 安全的转化日期字符串.
     * @param value 格式为'yyyy-MM-dd HH:mm:ss'或'yyyy-MM-dd HH:mm'或'yyyy-MM-dd'
     * @return
     */
    public static Date toDate(final String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String[] dateFormats = new String[] {
                "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "yyyy-MM-dd HH:mm" };

        for (String dateFormat : dateFormats) {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            try {
                return sdf.parse(value);
            } catch (ParseException e) {
                // do nothing.
            }
        }
        return null;
    }
}
