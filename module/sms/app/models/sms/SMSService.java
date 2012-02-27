package models.sms;

import play.modules.rabbitmq.producer.RabbitMQPublisher;

import java.util.List;

/**
 * User: likang
 */
public class SMSService {
    public static void send(String content, String phoneNumber){
        RabbitMQPublisher.publish("send_sms", new SMSMessage(content, phoneNumber));
    }
    public static void send(String content, List<String> phoneNumbers){
        RabbitMQPublisher.publish("send_sms", new SMSMessage(content, phoneNumbers));
    }
}
