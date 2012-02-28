package models.sms;

import play.Play;
import play.modules.rabbitmq.producer.RabbitMQPublisher;

import java.util.List;

/**
 * User: likang
 */
public class SMSService {
    private static final String queueName = Play.mode.isProd() ? "send_sms" : "send_sms_dev";
    public static void send(String content, String phoneNumber){
        RabbitMQPublisher.publish(queueName, new SMSMessage(content, phoneNumber));
    }
    public static void send(String content, List<String> phoneNumbers){
        RabbitMQPublisher.publish(queueName, new SMSMessage(content, phoneNumbers));
    }
}
