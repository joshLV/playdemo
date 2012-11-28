package models.taobao_coupon;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.libs.Codec;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author likang
 *         Date: 12-11-27
 */
public class TaobaoCouponUtil {
    public static final String COUPON_SECRET = Play.configuration.getProperty("taobao.coupon.secret", "");

    public static boolean verifyParam(Map<String, String> params) {
        params.remove("body");
        String sign = params.remove("sign");
        TreeMap<String, String> orderedParams = new TreeMap<>(params);
        StringBuilder paramString = new StringBuilder(COUPON_SECRET);
        for (String key : orderedParams.keySet()) {
            String value = orderedParams.get(key);
            if (!StringUtils.isBlank(value)) {
                paramString.append(key).append(value);
            }
        }
        try {
            byte[] paramBytes = paramString.toString().getBytes("GBK");
            String calculatedSign = new String(DigestUtils.md5(paramBytes), "GBK");
            return calculatedSign.equals(sign);
        }catch (UnsupportedEncodingException e) {
            return false;
        }
    }


}
