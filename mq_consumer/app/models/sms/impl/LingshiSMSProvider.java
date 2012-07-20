package models.sms.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import models.sms.SMSException;
import models.sms.SMSMessage;
import models.sms.SMSProvider;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
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

public class LingshiSMSProvider implements SMSProvider {

    private final String SEND_URL = Play.configuration.getProperty("lingshi.http.send_url");
    private final String USERNAME = Play.configuration.getProperty("lingshi.http.username");
    private final String PASSWORD = Play.configuration.getProperty("lingshi.http.password");

    private final Pattern RESULTCODE_PATTERN = Pattern.compile("<code>(\\d+)</code>");
    @Override
    public int send(SMSMessage message) {
        int resultCode = 0;
        //准备url
        String phoneArgs = StringUtils.join(message.getPhoneNumbers(), ",");

        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("OperID", USERNAME));
        qparams.add(new BasicNameValuePair("OperPass", PASSWORD));
        qparams.add(new BasicNameValuePair("AppendID", message.getCode()));
        qparams.add(new BasicNameValuePair("ContentType", "15"));
        /*
        try {
            qparams.add(new BasicNameValuePair("Content", URLEncoder.encode(message.getContent(), "GBK")));
        } catch (UnsupportedEncodingException e1) {
            Logger.warn("发送:(" + message.getContent() + ")时转码失败");
            qparams.add(new BasicNameValuePair("Content", message.getContent()));
        }
        */
        qparams.add(new BasicNameValuePair("Content", message.getContent()));
        qparams.add(new BasicNameValuePair("DesMobile", phoneArgs));
        String url = SEND_URL.replace(":sms_info", URLEncodedUtils.format(qparams, "GBK"));

        //准备http请求
        AbstractHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        HttpResponse response = null;

        System.out.println("url=" + url + "++++++++++++++++");
        Logger.debug("************ LingshiSMS: request url:"  + url + "*************");

        try {
            response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //读取响应
                String  result = inputStream2String(entity.getContent());
                Logger.info("返回消息：" + result);
                Matcher m = RESULTCODE_PATTERN.matcher(result);
                if (m.find()) {
                    //发送成功
                    String code = m.group(0);
                    resultCode = Integer.parseInt(code);
                    httpget.abort();
                    if (resultCode != 0 && resultCode != 1 && resultCode != 3) {
                         throw new SMSException(resultCode, result);
                    }
                } else {
                    //发送失败
                    httpget.abort();
                    throw new SMSException(-103, "发送结果不匹配");
                }
            } else {
                //无响应
                throw new SMSException(-102, "无响应");
            }

        } catch (Exception e) {
            e.printStackTrace();
            //http请求失败
            throw new SMSException(-105, e);
        }
        return resultCode;
    }

    @Override
    public String getProviderName() {
        return "BjenSMSProvider";
    }

    private String inputStream2String(InputStream in) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

}
