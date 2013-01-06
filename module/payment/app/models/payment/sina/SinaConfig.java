package models.payment.sina;

import play.Play;

/**
 * @author likang
 *         Date: 13-1-6
 */
public class SinaConfig {

    public static final String APP_ID = Play.configuration.getProperty("sina.pay.merchant_acct_id","1002034040901");
    public static final String PRIVATE_KEY_PATH = Play.configuration.getProperty("sina.pay.private_key");
    public static final String PUBLIC_KEY_PATH  = Play.configuration.getProperty("sina.pay.public_key");
    public static final String KEY_PWD = Play.configuration.getProperty("sina.pay.key_pwd","yu@uhuila.seewi");
    public static final String NOTIFY_URL = Play.configuration.getProperty("sina.pay.notify_url");
    public static final String RETURN_URL = Play.configuration.getProperty("sina.pay.return_url");
    public static final String SERVER_URL = "https://gate.pay.sina.com.cn/acquire-order-channel/gateway/receiveOrderLoading.htm";
}
