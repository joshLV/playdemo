package models.payment.sina;

import play.Play;

/**
 * @author likang
 *         Date: 13-1-6
 */
public class SinaConfig {
    public static final String PRIVATE_KEY_PATH = Play.configuration.getProperty("sina.pay.private_key");
    public static final String PUBLIC_KEY_PATH  = Play.configuration.getProperty("sina.pay.public_key");

    public static final String MERCHANT_ACCOUNT_ID = Play.configuration.getProperty("sina.pay.merchant_account_id");
    public static final String MEMBER_ID = Play.configuration.getProperty("sina.pay.member_id");
    public static final String NOTIFY_URL = Play.configuration.getProperty("sina.pay.notify_url");
    public static final String RETURN_URL = Play.configuration.getProperty("sina.pay.return_url");
    public static final String SERVER_URL = Play.configuration.getProperty("sina.pay.gate");
    public static final String WAP_SERVER_URL = Play.configuration.getProperty("sina.pay.wap_gate");
}
