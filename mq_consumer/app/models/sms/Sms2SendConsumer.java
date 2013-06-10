package models.sms;

import models.mq.RabbitMQConsumerWithTx;
import play.Logger;
import play.Play;
import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.RabbitMQPlugin;


/**
 * User: likang
 */
@OnApplicationStart(async = true)
public class Sms2SendConsumer extends RabbitMQConsumerWithTx<SMSMessage> {
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
        if (smsProvider2 == null) {
            smsProvider2 = SMSFactory.getSMSProvider(SMS_TYPE2);
        }
        return smsProvider2;
    }

    @Override
    public void consumeWithTx(SMSMessage message) {
        if (SMS_TYPE == null) {
            Logger.error("Sms2SendConsumer: can not get the SMS_TYPE in application.conf");
            return;
        }

        try {
            getSMSProvider2().send(message);
        } catch (Exception e) {
            Logger.info("Sms2SendConsumer: Send SMS failed use " + SMS_TYPE + ", try " + SMS_TYPE2);
            getSMSProvider().send(message);
        }
    }

    @Override
    protected String queue() {
        return SMSMessage.SMS2_QUEUE;

    }

    @Override
    protected String routingKey() {
        return this.queue();
    }

    @Override
    protected int retries() {
        // This is the default value defined by "rabbitmq.retries” on
        // application.conf (please override if you need a new value)
        return RabbitMQPlugin.retries();
    }

    @Override
    protected Class getMessageType() {
        return SMSMessage.class;
    }
}


