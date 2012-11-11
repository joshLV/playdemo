package models.sms;

import models.journal.MQJournal;
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
            smsProvider = SMSFactory.getSMSProvider(smsType);
        }
        return smsProvider;
    }

    @Override
    protected void consume(SMSMessage message) {
        if (SMS_TYPE == null) {
            return;
        }
        
        try {
            getSMSProvider(SMS_TYPE).send(message);
        } catch (SMSException e) {
            Logger.error("SmsSenderConsumer: send message" + message + " failed:" + e.getMessage());
            throw e;
        }
    }

    protected String queue() {
        return SMSUtil.SMS_QUEUE;

    }

    protected String routingKey() {
        return this.queue();
    }

    /**
     * 重试次数.
     */
    protected int retries() {
        // This is the default value defined by "rabbitmq.retries” on
        // application.conf (please override if you need a new value)
        return RabbitMQPlugin.retries();
    }

    protected Class getMessageType() {
        return SMSMessage.class;
    }
}


