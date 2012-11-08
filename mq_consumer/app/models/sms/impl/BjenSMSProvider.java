package models.sms.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
import org.apache.ivy.util.url.HttpClientHandler.HttpClientHelper;

import play.Logger;
import play.Play;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 北京奥软短信接口实现
 * @author <a href="mailto:tangliqun@uhuila.com">唐力群</a>
 *
 */
public class BjenSMSProvider implements SMSProvider {

    private final String SEND_URL = Play.configuration.getProperty("ensms.http.send_url");
    private final String USERNAME = Play.configuration.getProperty("ensms.http.username");
    private final String PASSWORD = Play.configuration.getProperty("ensms.http.password");    

    @Override
    public int send(SMSMessage message) {
        int resultCode = 0;
        //准备url
        String phoneArgs = StringUtils.join(message.getPhoneNumbers(), ",");

        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("username", USERNAME));
        long dt = System.currentTimeMillis()/1000;
        qparams.add(new BasicNameValuePair("pwd", generateMd5Password(USERNAME, PASSWORD, dt)));
        qparams.add(new BasicNameValuePair("dt", String.valueOf(dt)));
        qparams.add(new BasicNameValuePair("code", message.getCode()));
        qparams.add(new BasicNameValuePair("msg", message.getContent()));
        qparams.add(new BasicNameValuePair("mobiles", phoneArgs));
        String url = SEND_URL.replace(":sms_info", URLEncodedUtils.format(qparams, "UTF-8"));

        //准备http请求
        AbstractHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        HttpResponse response = null;

        Logger.debug("************ SmsSendConsumer: request url:"  + url + "*************");

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
        } finally {
        	httpclient.getConnectionManager().shutdown();
        }
        return resultCode;
    }

    @Override
    public String getProviderName() {
        return "BjenSMSProvider";
    }


    /**
     * 生成小写的密码串。
     * 用户密码md5(用户名+密码+时间戳)
     * 比如用户名为wang 密码为 qiqi那密码为：md5(wangqiqi1319873904)
     * url传送密码为5a1a023fd486e2f0edbc595854c0d808
     * @param username
     * @param password
     * @param dt
     * @return
     */
    public String generateMd5Password(String username, String password, long dt) {
        String pwd = username + password + String.valueOf(dt);
        return DigestUtils.md5Hex(pwd);
    }
}
