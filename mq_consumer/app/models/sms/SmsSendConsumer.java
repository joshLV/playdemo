package models.sms;

import models.RabbitMQConsumerWithTx;
import play.Logger;
import play.Play;
import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.RabbitMQPlugin;


/**
 * User: likang
 */
@OnApplicationStart(async = true)
public class SmsSendConsumer extends RabbitMQConsumerWithTx<SMSMessage> {

    private final String SMS_TYPE = Play.configuration.getProperty("sms.type");
    private final String SMS_TYPE2 = Play.configuration.getProperty("sms2.type");

    private SMSProvider smsProvider = null;
    private SMSProvider smsProvider2 = null;

    public SMSProvider getSMSProvider() {
        if (smsProvider == null) {
            smsProvider = SMSFactory.getSMSProvider(SMS_TYPE);
        }
        return smsProvider;
    }
    public SMSProvider getSMSProvider2() {
        if (smsProvider == null) {
            smsProvider = SMSFactory.getSMSProvider(SMS_TYPE2);
        }
        return smsProvider;
    }

    @Override
    public void consumeWithTx(SMSMessage message) {
        Logger.info("send message by sms.type:" + SMS_TYPE);
        if (SMS_TYPE == null) {
            return;
        }
        
        try {
            getSMSProvider().send(message);
        } catch (SMSException e) {
            Logger.info("SmsSendConsumer: Send SMS failed use " + SMS_TYPE + ", try " + SMS_TYPE2);
            getSMSProvider2().send(message);
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


