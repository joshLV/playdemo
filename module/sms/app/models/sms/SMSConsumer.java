package models.sms;

import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.RabbitMQPlugin;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;


/**
 * User: likang
 */
@OnApplicationStart(async = true)
public class SMSConsumer extends RabbitMQConsumer<SMSMessage> {

    protected void consume(SMSMessage message) {
        System.out.println("******************************");

        System.out.println("* Message Consumed: text:" + message.getContent());

        System.out.println("******************************");
    }

    protected String queue() {
        return "send_sms";
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


