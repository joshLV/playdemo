package models.payment.kuaiqian;

import play.Play;

/**
 * @author likang
 * Date: 12-5-23
 */
public class KuaiqianConfig {
    public static final String APP_ID = Play.configuration.getProperty("99bill.merchant_acct_id","1002034040901");
    public static final String PRIVATE_KEY_PATH = Play.configuration.getProperty("99bill.private_key");
    public static final String PUBLIC_KEY_PATH  = Play.configuration.getProperty("99bill.public_key");
    public static final String KEY_PWD = Play.configuration.getProperty("99bill.key_pwd","yu@uhuila.seewi");
    public static final String NOTIFY_URL = Play.configuration.getProperty("99bill.notify_url");
    public static final String RETURN_URL = Play.configuration.getProperty("99bill.return_url");
    public static final String SERVER_URL = "https://www.99bill.com/gateway/recvMerchantInfoAction.htm";
}
