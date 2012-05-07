package models.sms;

import play.Play;
import play.modules.rabbitmq.producer.RabbitMQPublisher;

import java.util.List;

/**
 * User: likang
 */
public class SMSUtil {
    public static final String QUEUE_NAME = Play.mode.isProd() ? "send_sms" : "send_sms_dev";
    
    public static final String SMS_QUEUE2 = Play.mode.isProd() ? "send_sms2" : "send_sms_dev2";
    
    private SMSUtil(){}
    public static void send(String content, String phoneNumber){
        RabbitMQPublisher.publish(SMS_QUEUE2, new SMSMessage(content, phoneNumber));
    }
    public static void send(String content, List<String> phoneNumbers){
        RabbitMQPublisher.publish(SMS_QUEUE2, new SMSMessage(content, phoneNumbers));
    }
}
