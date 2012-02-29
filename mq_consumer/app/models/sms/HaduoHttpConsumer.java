package models.sms;

import models.journal.MQJournal;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * User: likang
 */
@OnApplicationStart(async = true)
public class HaduoHttpConsumer extends RabbitMQConsumer<SMSMessage> {
    private final String SEND_URL = Play.configuration.getProperty("sms.haduo.send_url");
    private static final String QUEUE_NAME = Play.mode.isProd() ? "send_sms" : "send_sms_dev";


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
            new MQJournal(QUEUE_NAME, message.getContent() + " | " + phone + " | " + status + " | " + serial).save();
        }
        JPAPlugin.closeTx(false);
    }

    @Override
    protected void consume(SMSMessage message) {
        if (SEND_URL == null) {
            saveJournal(message, "-14", null);
            Logger.error("SMSHaduoHttpConsumer: can not get the SEND_URL in application.conf");
            return;
        }
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
                    saveJournal(message, "0", line);
                    httpget.abort();
                } else {
                    //发送失败
                    saveJournal(message, line, null);
                    httpget.abort();
                }
            } else {
                //无响应
                saveJournal(message, "-12", null);
            }

        } catch (IOException e) {
            //http请求失败
            saveJournal(message, "-13", null);
        }
    }

    protected String queue() {
        return QUEUE_NAME;

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


