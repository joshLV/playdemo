package models.sms.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import models.sms.SMSMessage;
import models.sms.SMSProvider;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import play.Logger;
import play.Play;

public class HaduoHttpSMSProvider implements SMSProvider {

    private final String SEND_URL = Play.configuration.getProperty("sms.http.send_url");
    
    @Override
    public int send(SMSMessage message) {
        
        int resultCode = 0;
        //准备url
        StringBuffer phonesBuffer = new StringBuffer();
        for (String phone : message.getPhoneNumbers()) {
            phonesBuffer.append(phone).append(";");
        }
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("content", message.getContent()));
        qparams.add(new BasicNameValuePair("phonestr", phonesBuffer.toString()));
        String url = SEND_URL.replace(":sms_info", URLEncodedUtils.format(qparams, "UTF-8"));
        
        

        //准备http请求
        AbstractHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        HttpResponse response = null;

        Logger.debug("************ SMSHaduoHttpConsumer: request url:"  + url);

        try {
            response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //读取响应
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(entity.getContent()));
                String line = bufferedReader.readLine();
                if (line.equals("0")) {
                    //发送成功
                    line = bufferedReader.readLine();
                    resultCode = 0;
                    httpget.abort();
                } else {
                    //发送失败
                    resultCode = Integer.parseInt(line);
                    httpget.abort();
                }
            } else {
                //无响应
                resultCode = -12;
            }

        } catch (IOException e) {
            //http请求失败
            resultCode = -13;
        }
        return resultCode;
    }

    @Override
    public String getProviderName() {
        return "HaduoHttp";
    }

}
