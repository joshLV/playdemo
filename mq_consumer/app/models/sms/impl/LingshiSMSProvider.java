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
import util.ws.WebServiceRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 上海领时网络接口.
 *
 * @author <a href="mailto:tangliqun@uhuila.com">唐力群</a>
 */
public class LingshiSMSProvider implements SMSProvider {

    private final String SEND_URL = Play.configuration
                    .getProperty("lingshi.http.send_url");
    private final String USERNAME = Play.configuration
                    .getProperty("lingshi.http.username");
    private final String PASSWORD = Play.configuration
                    .getProperty("lingshi.http.password");

    private final Pattern RESULTCODE_PATTERN = Pattern
                    .compile("<code>(\\d+)</code>");

    @Override
    public void send(SMSMessage message) {
        int resultCode = 0;
        // 准备url
        String phoneArgs = StringUtils.join(message.getPhoneNumbers(), ",");

        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("OperID", USERNAME));
        qparams.add(new BasicNameValuePair("OperPass", PASSWORD));
        qparams.add(new BasicNameValuePair("AppendID", message.getCode()));
        qparams.add(new BasicNameValuePair("ContentType", "15"));
        /*
         * try { qparams.add(new BasicNameValuePair("Content",
         * URLEncoder.encode(message.getContent(), "GBK"))); } catch
         * (UnsupportedEncodingException e1) { Logger.warn("发送:(" +
         * message.getContent() + ")时转码失败"); qparams.add(new
         * BasicNameValuePair("Content", message.getContent())); }
         */
        qparams.add(new BasicNameValuePair("Content", message.getContent()));
        qparams.add(new BasicNameValuePair("DesMobile", phoneArgs));
        String url = SEND_URL.replace(":sms_info",
                        URLEncodedUtils.format(qparams, "GBK"));

        String result = WebServiceRequest.url(url).type("LingshiSMS").addKeyword(phoneArgs).getString();

        Logger.info("返回消息：" + result);
        Matcher m = RESULTCODE_PATTERN.matcher(result);
        if (m.find()) {
            // 发送成功
            String code = m.group(1);
            resultCode = Integer.parseInt(code);
            if (resultCode != 0 && resultCode != 1 && resultCode != 3) {
                throw new SMSException(result);
            }
        } else {
            // 发送失败
            throw new SMSException("发送结果不匹配");
        }
    }

    @Override
    public String getProviderName() {
        return "LingshiSMSProvider";
    }

}
