package models.sms.impl;


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

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
    public int send(SMSMessage message) {
        int resultCode = 0;
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

        //准备http请求
        AbstractHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        HttpResponse response = null;

        Logger.debug("************ Sms2SendConsumer: request url:"  + url + "*************");

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
                    resultCode = Integer.parseInt(line.trim());
                    httpget.abort();
                    throw new SMSException(resultCode, "发送失败");
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
