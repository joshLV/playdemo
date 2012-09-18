package models.sms;

import models.sms.impl.BjenSMSProvider;
import models.sms.impl.C123HttpSMSProvider;
import models.sms.impl.HaduoHttpSMSProvider;
import models.sms.impl.LingshiSMSProvider;
import models.sms.impl.Tui3SMSProvider;
import models.sms.impl.ZtSMSProvider;

/**
 * 短信提供商工厂类.
 * 
 * @author <a href="mailto:tangliqun@uhuila.com">唐力群</a>
 */
public class SMSFactory {

    public static SMSProvider getSMSProvider(String sysType) {
        SMSProvider smsProvider = null;

        if ("tui3".equalsIgnoreCase(sysType)) {
            smsProvider = new Tui3SMSProvider();
        } else if ("ensms".equalsIgnoreCase(sysType)) {
            smsProvider = new BjenSMSProvider();
        } else if ("ztsms".endsWith(sysType)) {
            smsProvider = new ZtSMSProvider();
        } else if ("c123".endsWith(sysType)) {
            smsProvider = new C123HttpSMSProvider();
        } else if ("haduo".endsWith(sysType)) {
            smsProvider = new HaduoHttpSMSProvider();
        } else if ("lingshi".endsWith(sysType)) {
            smsProvider = new LingshiSMSProvider();
        } else {
            smsProvider = new MockSMSProvider();
        }

        return smsProvider;
    }

}
