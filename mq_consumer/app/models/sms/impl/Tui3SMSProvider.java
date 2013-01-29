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

public class Tui3SMSProvider implements SMSProvider {

    private final String SEND_URL = Play.configuration
                    .getProperty("tui3.http.send_url");
    private final String API_KEY = Play.configuration
                    .getProperty("tui3.http.api_key");

    private final Pattern RESULTCODE_PATTERN = Pattern
                    .compile("<err_code>(\\d+)</err_code>");

    @Override
    public void send(SMSMessage message) {
        // 准备url
        String phoneArgs = StringUtils.join(message.getPhoneNumbers(), ",");

        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("k", API_KEY));
        qparams.add(new BasicNameValuePair("p", "1")); // 短信产品, 1表示推信
        qparams.add(new BasicNameValuePair("r", "xml")); // 结果格式， xml
        qparams.add(new BasicNameValuePair("c", message.getContent()));
        qparams.add(new BasicNameValuePair("s", message.getCode()));
        qparams.add(new BasicNameValuePair("t", phoneArgs));
        String url = SEND_URL.replace(":sms_info",
                        URLEncodedUtils.format(qparams, "UTF-8"));

        String result = WebServiceRequest.url(url).type("Tui3SMS").addKeyword(phoneArgs).getString();

        Logger.info("返回消息：" + result);
        Matcher m = RESULTCODE_PATTERN.matcher(result);
        if (m.find()) {
            // 发送成功
            String code = m.group(1);
            int resultCode = Integer.parseInt(code);
            if (resultCode != 0 && resultCode != 1 && resultCode != 3) {
                throw new SMSException(result);
            }
        } else {
            // 发送失败
            throw new SMSException(-103, "发送结果不匹配");
        }

    }

    @Override
    public String getProviderName() {
        return "Tui3SMSProvider";
    }

}
