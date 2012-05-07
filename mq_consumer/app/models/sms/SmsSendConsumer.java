package models.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import models.journal.MQJournal;
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
import play.db.jpa.JPAPlugin;
import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.RabbitMQPlugin;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;


/**
 * User: likang
 */
@OnApplicationStart(async = true)
public class SmsSendConsumer extends RabbitMQConsumer<SMSMessage> {
    private final String SEND_URL = Play.configuration.getProperty("sms.http.send_url");
    private final String USERNAME = Play.configuration.getProperty("sms.http.username");
    private final String PASSWORD = Play.configuration.getProperty("sms.http.password");


    /**
     * 保存发送记录
     *
     * @param message 消息
     * @param status  状态
     * @param serial  成功序列号
     */
    private void saveJournal(SMSMessage message, String status, String serial) {
        JPAPlugin.startTx(false);
        for (String phone : message.getPhoneNumbers()) {
            new MQJournal(SMSUtil.SMS_QUEUE2, message.getContent() + " | " + phone + " | " + status + " | " + serial).save();
        }
        JPAPlugin.closeTx(false);
    }

    @Override
    protected void consume(SMSMessage message) {
        if (SEND_URL == null) {
            saveJournal(message, "-101", null);
            Logger.error("SmsSendConsumer: can not get the SEND_URL in application.conf");
            return;
        }
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

        System.out.println("url=" + url + "++++++++++++++++");
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
                    saveJournal(message, "0", line);
                    httpget.abort();
                } else {
                    //发送失败
                    saveJournal(message, line, null);
                    httpget.abort();
                }
            } else {
                //无响应
                saveJournal(message, "-102", null);
            }

        } catch (IOException e) {
            e.printStackTrace();
            //http请求失败
            saveJournal(message, "-105", null);
        }
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

    protected String queue() {
        return SMSUtil.SMS_QUEUE2;

    }

    protected String routingKey() {
        return this.queue();
    }

    protected int retries() {
        // This is the default value defined by "rabbitmq.retries” on
        // application.conf (please override if you need a new value)
        return RabbitMQPlugin.retries();
    }

    protected Class getMessageType() {
        return SMSMessage.class;
    }
}


