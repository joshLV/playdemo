package models.sms;

import models.sms.impl.BjenSMSProvider;
import models.sms.impl.C123HttpSMSProvider;
import models.sms.impl.HaduoHttpSMSProvider;
import models.sms.impl.LingshiSMSProvider;
import models.sms.impl.Tui3SMSProvider;
import models.sms.impl.VxSMSProvider;
import models.sms.impl.ZtSMSProvider;
import play.Logger;

/**
 * 短信提供商工厂类.
 * 
 * @author <a href="mailto:tangliqun@uhuila.com">唐力群</a>
 */
public class SMSFactory {

    public static SMSProvider getSMSProvider(String smsType) {
        SMSProvider smsProvider = null;

        if ("tui3".equalsIgnoreCase(smsType)) {
            smsProvider = new Tui3SMSProvider();
        } else if ("ensms".equalsIgnoreCase(smsType)) {
            smsProvider = new BjenSMSProvider();
        } else if ("ztsms".endsWith(smsType)) {
            smsProvider = new ZtSMSProvider();
        } else if ("c123".endsWith(smsType)) {
            smsProvider = new C123HttpSMSProvider();
        } else if ("haduo".endsWith(smsType)) {
            smsProvider = new HaduoHttpSMSProvider();
        } else if ("lingshi".endsWith(smsType)) {
            smsProvider = new LingshiSMSProvider();
        } else if ("vxsms".endsWith(smsType)) {
            smsProvider = new VxSMSProvider();
        } else {
            Logger.error("NOT set SMSType");
            smsProvider = new VxSMSProvider();
        }

        return smsProvider;
    }

}
