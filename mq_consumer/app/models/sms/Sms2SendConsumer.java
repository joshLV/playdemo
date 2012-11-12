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
public class Sms2SendConsumer extends RabbitMQConsumerWithTx<SMSMessage> {
    private final String SMS_TYPE    = Play.configuration
                                             .getProperty("sms2.type");

    private SMSProvider smsProvider = null;
 
    public SMSProvider getSMSProvider(String smsType) {
        if (smsProvider == null) {
            smsProvider = SMSFactory.getSMSProvider(smsType);
        }
        return smsProvider;
    }

    @Override
    public void consumeWithTx(SMSMessage message) {
        if (SMS_TYPE == null) {
            Logger.error("Sms2SendConsumer: can not get the SMS_TYPE in application.conf");
            return;
        }

        try {
            getSMSProvider(SMS_TYPE).send(message);
        } catch (SMSException e) {
            Logger.error("Sms2SenderConsumer: send message" + message + " failed:" + e.getMessage());
            throw e;
        }
    }

    @Override
    protected String queue() {
        return SMSUtil.SMS2_QUEUE;

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


