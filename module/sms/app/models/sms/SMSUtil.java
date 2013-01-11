package models.sms;

import play.Play;
import util.mq.MQPublisher;

import java.util.Arrays;
import java.util.List;

/**
 * User: likang
 */
public class SMSUtil {
    
    // 短信MQ名称，在测试模式加入一个时间戳，以避免被其它进程消费.
    public static final String SMS_QUEUE = Play.mode.isProd() ? "send_sms" : "send_sms_dev";
    
    // 短信MQ名称，在测试模式加入一个时间戳，以避免被其它进程消费.
    public static final String SMS2_QUEUE = Play.mode.isProd() ? "send_sms2" : "send_sms2_dev";

    public static final String SMS_ORDER_QUEUE = "send_order_sms";
    
    private SMSUtil(){}
    
    public static void send(String content, String phoneNumber, String code){
        MQPublisher.publish(SMS_QUEUE, new SMSMessage(content, phoneNumber, code));
    }

    public static void send(String content, String phoneNumber){
        MQPublisher.publish(SMS_QUEUE, new SMSMessage(content, phoneNumber));
    }
    
    public static void send(String content, String[] phoneNumbers){
        MQPublisher.publish(SMS_QUEUE, new SMSMessage(content, Arrays.asList(phoneNumbers)));
    }
        
    public static void send2(String content, String phoneNumber, String code){
        MQPublisher.publish(SMS2_QUEUE, new SMSMessage(content, phoneNumber, code));
    }
    public static void send2(String content, List<String> phoneNumbers){
        MQPublisher.publish(SMS2_QUEUE, new SMSMessage(content, phoneNumbers));
    }

    public static void sendOrderSms(Long orderId) {
        MQPublisher.publish(SMS_ORDER_QUEUE, orderId);
    }
    
}
