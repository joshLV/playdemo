package models.sms.impl;

import models.sms.SMSException;
import models.sms.SMSMessage;
import models.sms.SMSProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import play.Logger;
import play.Play;
import util.ws.WebServiceClient;
import util.ws.WebServiceClientFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 上海助通网络接口.
 * 
 * @author <a href="mailto:tangliqun@uhuila.com">唐力群</a>
 */
public class ZtSMSProvider implements SMSProvider {

    private final String SEND_URL = Play.configuration
                    .getProperty("ztsms.http.send_url");
    private final String USERNAME = Play.configuration
                    .getProperty("ztsms.http.username");
    private final String PASSWORD = Play.configuration
                    .getProperty("ztsms.http.password");

    private final Pattern RESULTCODE_PATTERN = Pattern.compile("^1,");

    @Override
    public void send(SMSMessage message) {
        List<String> avaiablePhoneNumbers = new ArrayList<>();
        for (String phone : message.getPhoneNumbers()) {
            if (phone != null && phone.length() >= 11) { // 手机号必须大于等于11位
                avaiablePhoneNumbers.add(phone);
            }
        }

        if (avaiablePhoneNumbers.size() == 0) {
            Logger.info("手机号位数不足：" + message);
            return;
        }

        // 准备url
        String phoneArgs = StringUtils.join(message.getPhoneNumbers(), ",");

        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("username", USERNAME));
        qparams.add(new BasicNameValuePair("password", PASSWORD));
        // FIXME: 助通要求xh只能2位
        String xh = message.getCode(); 
        if (StringUtils.isNotBlank(xh)) {
            xh = xh.substring(0, 2);
        }

        qparams.add(new BasicNameValuePair("xh", xh));
        qparams.add(new BasicNameValuePair("productid", "887361"));

        qparams.add(new BasicNameValuePair("content", message.getContent()));
        qparams.add(new BasicNameValuePair("mobile", phoneArgs));
        String url = SEND_URL.replace(":sms_info",
                        URLEncodedUtils.format(qparams, "UTF-8"));

        WebServiceClient client = WebServiceClientFactory
                        .getClientHelper();

        String result = client.getString("ZTSMS", url, phoneArgs);
        Logger.info("返回消息：" + result);
        result = result.trim();
        Matcher m = RESULTCODE_PATTERN.matcher(result);
        if (!m.find()) {
            if (result.startsWith("13")) {
                Logger.info("忽略13：30分钟内重复发送.");
            } else {
                // 发送失败
                throw new SMSException("发送助通短信不成功:" + result);
            }
        }

    }

    @Override
    public String getProviderName() {
        return "ZtSMSProvider";
    }

}
