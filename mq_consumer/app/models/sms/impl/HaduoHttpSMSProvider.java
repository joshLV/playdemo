package models.sms.impl;

import models.sms.SMSException;
import models.sms.SMSMessage;
import models.sms.SMSProvider;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import play.Play;
import util.ws.WebServiceRequest;

import java.util.ArrayList;
import java.util.List;

public class HaduoHttpSMSProvider implements SMSProvider {

    private final String SEND_URL = Play.configuration.getProperty("sms.http.send_url");

    @Override
    public void send(SMSMessage message) {
        //准备url
        StringBuffer phonesBuffer = new StringBuffer();
        for (String phone : message.getPhoneNumbers()) {
            phonesBuffer.append(phone).append(";");
        }
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("content", message.getContent()));
        qparams.add(new BasicNameValuePair("phonestr", phonesBuffer.toString()));
        String url = SEND_URL.replace(":sms_info", URLEncodedUtils.format(qparams, "UTF-8"));

        String line = WebServiceRequest.url(url).type("HaduoSMS").addKeyword(phonesBuffer).getString();

        if (!line.equals("0")) {
            // 发送失败
            throw new SMSException("发送失败");
        }
    }

    @Override
    public String getProviderName() {
        return "HaduoHttp";
    }

}
