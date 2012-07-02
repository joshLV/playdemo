package models.sms;

import models.journal.MQJournal;
import models.sms.impl.BjenSMSProvider;
import models.sms.impl.C123HttpSMSProvider;
import models.sms.impl.HaduoHttpSMSProvider;
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
    private final String SMS_TYPE = Play.configuration.getProperty("sms.type");
    
    private SMSProvider smsProvider = null;
    
    public SMSProvider getSMSProvider(String smsType) {
        if (smsProvider == null) {
            if ("ensms".equalsIgnoreCase(SMS_TYPE)) {
                smsProvider = new BjenSMSProvider(); 
            } else if ("c123".endsWith(SMS_TYPE)) {
                smsProvider = new C123HttpSMSProvider();
            } else if ("haduo".endsWith(SMS_TYPE)) {
                smsProvider = new HaduoHttpSMSProvider();
            } else {
                smsProvider = new MockSMSProvider();
            }
        }
        return smsProvider;
    }

    /**
     * 保存发送记录
     *
     * @param message 消息
     * @param status  状态
     * @param serial  成功序列号
     */
    private void saveJournal(SMSMessage message, int status, String serial) {
        JPAPlugin.startTx(false);
        for (String phone : message.getPhoneNumbers()) {
            new MQJournal(SMSUtil.SMS_QUEUE, message.getContent() + " | " + phone + " | " + status + " | " + serial).save();
        }
        JPAPlugin.closeTx(false);
    }

    @Override
    protected void consume(SMSMessage message) {
        if (SMS_TYPE == null) {
            saveJournal(message, -101, null);
            Logger.error("SmsSendConsumer: can not get the SMS_TYPE in application.conf");
            return;
        }
        
        try {
            int resultCode = getSMSProvider(SMS_TYPE).send(message);
            saveJournal(message, resultCode, null);        
        } catch (SMSException e) {
            Logger.error("SmsSenderConsumer: send message" + message + " failed:" + e.getResultCode());
            saveJournal(message, e.getResultCode(), e.getMessage());        
            throw e;
        }
    }

    protected String queue() {
        return SMSUtil.SMS_QUEUE;

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


