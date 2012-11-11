package models.sms.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.sms.SMSException;
import models.sms.SMSMessage;
import models.sms.SMSProvider;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import play.Logger;
import play.Play;
import util.ws.WebServiceClientFactory;
import util.ws.WebServiceClient;

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
        // 准备url
        String phoneArgs = StringUtils.join(message.getPhoneNumbers(), ",");

        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("username", USERNAME));
        qparams.add(new BasicNameValuePair("password", PASSWORD));
        qparams.add(new BasicNameValuePair("xh", message.getCode()));
        qparams.add(new BasicNameValuePair("productid", "887361"));
        /*
         * try { qparams.add(new BasicNameValuePair("Content",
         * URLEncoder.encode(message.getContent(), "GBK"))); } catch
         * (UnsupportedEncodingException e1) { Logger.warn("发送:(" +
         * message.getContent() + ")时转码失败"); qparams.add(new
         * BasicNameValuePair("Content", message.getContent())); }
         */
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
            // 发送失败
            throw new SMSException("发送助通短信不成功:" + result);
        }

    }

    @Override
    public String getProviderName() {
        return "ZtSMSProvider";
    }

}
