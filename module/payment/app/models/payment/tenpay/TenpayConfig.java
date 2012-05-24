package models.payment.tenpay;

import play.Play;

/**
 * @author 12-5-22
 * Time: 下午7:26
 */
public class TenpayConfig {
    public static final String APP_ID = Play.configuration.getProperty("tenpay.app_id","1211869101");
    public static final String SECRET_KEY = Play.configuration.getProperty("tenpay.key","cdaae5034d86383038eddcd1b8834c89");
    public static final boolean IN_SANDBOX = !"false".equals(Play.configuration.getProperty("tenpay.in_sandbox","false"));
    public static final String NOTIFY_URL = Play.configuration.getProperty("tenpay.notify_url");
    public static final String RETURN_URL = Play.configuration.getProperty("tenpay.return_url");
}
