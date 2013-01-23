package models.sms.impl;


import models.sms.SMSException;
import models.sms.SMSMessage;
import models.sms.SMSProvider;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import play.Play;
import util.ws.WebServiceRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * C123
 * @author <a href="mailto:tangliqun@uhuila.com">唐力群</a>
 *
 */
public class C123HttpSMSProvider implements SMSProvider {

    private final String SEND_URL = Play.configuration.getProperty("c123.http.send_url");
    private final String USERNAME = Play.configuration.getProperty("c123.http.username");
    private final String PASSWORD = Play.configuration.getProperty("c123.http.password");

    @Override
    public void send(SMSMessage message) {
        //准备url
        String phoneArgs = StringUtils.join(message.getPhoneNumbers(), ",");

        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("uid", USERNAME));
        qparams.add(new BasicNameValuePair("pwd", generateMd5Password(USERNAME, PASSWORD)));
        qparams.add(new BasicNameValuePair("mid", message.getCode())); //扩展号，考虑去掉
        qparams.add(new BasicNameValuePair("encode", "utf8"));
        qparams.add(new BasicNameValuePair("content", message.getContent()));
        qparams.add(new BasicNameValuePair("mobile", phoneArgs));
        String url = SEND_URL.replace(":sms_info", URLEncodedUtils.format(qparams, "UTF-8"));

        String line = WebServiceRequest.url(url).type("C123SMS").addKeyword(phoneArgs).getString();
        if (!line.equals("0")) {
            // 发送失败
            throw new SMSException("发送失败");
        }

    }

    @Override
    public String getProviderName() {
        return "C123SMSProvider";
    }


    /**
     * 生成小写的密码串。
     * 用户密码md5(用户名+密码+时间戳)
     * @param username
     * @param password
     * @return
     */
    public String generateMd5Password(String username, String password) {
        return DigestUtils.md5Hex(password);
    }

}
