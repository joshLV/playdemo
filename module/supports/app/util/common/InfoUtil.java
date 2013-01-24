package util.common;

/**
 * User: tanglq
 * Date: 13-1-24
 * Time: 下午1:53
 */
public class InfoUtil {

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

    public static String getMaskedPhone(String phone) {
        StringBuilder sbPhone = new StringBuilder();
        sbPhone.append(phone.substring(0, 3));
        sbPhone.append("****");

        int len = phone.length();
        if (len > 10) {
            sbPhone.append(phone.substring(len - 4, len));
        }
        return sbPhone.toString();
    }

}
