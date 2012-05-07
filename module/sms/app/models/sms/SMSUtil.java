package models.sms;

import play.Play;
import play.modules.rabbitmq.producer.RabbitMQPublisher;

import java.util.List;
import models.order.ECoupon;

/**
 * User: likang
 */
public class SMSUtil {
    public static final String QUEUE_NAME = Play.mode.isProd() ? "send_sms" : "send_sms_dev";
    
    public static final String SMS_QUEUE2 = Play.mode.isProd() ? "send_sms2" : "send_sms_dev2";
    
    private SMSUtil(){}
    
    public static void send(String content, String phoneNumber, String code){
        RabbitMQPublisher.publish(QUEUE_NAME, new SMSMessage(content, phoneNumber, code));
    }
    public static void send(String content, List<String> phoneNumbers){
        RabbitMQPublisher.publish(QUEUE_NAME, new SMSMessage(content, phoneNumbers));
    }
    
    
    public static void send2(String content, String phoneNumber, String code){
        RabbitMQPublisher.publish(SMS_QUEUE2, new SMSMessage(content, phoneNumber, code));
    }
    public static void send2(String content, List<String> phoneNumbers){
        RabbitMQPublisher.publish(SMS_QUEUE2, new SMSMessage(content, phoneNumbers));
    }
}
