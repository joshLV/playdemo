package models.sms;

import play.Play;
import play.modules.rabbitmq.producer.RabbitMQPublisher;

import java.util.List;

/**
 * User: likang
 */
public class SMSUtil {
    
    // 短信MQ名称，在测试模式加入一个时间戳，以避免被其它进程消费.
    public static final String SMS_QUEUE = Play.mode.isProd() ? "send_sms" 
            : (Play.runingInTestMode() ? "send_sms_test_" + System.currentTimeMillis() : "send_sms_dev");
    
    
    private SMSUtil(){}
    
    public static void send(String content, String phoneNumber, String code){
        if (Play.runingInTestMode()) {
            MockSMSProvider mockSms = new MockSMSProvider();
            mockSms.send(new SMSMessage(content, phoneNumber, code));
        } else {
            RabbitMQPublisher.publish(SMS_QUEUE, new SMSMessage(content, phoneNumber, code));
        }
    }
    public static void send(String content, List<String> phoneNumbers){
        if (Play.runingInTestMode()) {
            MockSMSProvider mockSms = new MockSMSProvider();
            mockSms.send(new SMSMessage(content, phoneNumbers));
        } else {
            RabbitMQPublisher.publish(SMS_QUEUE, new SMSMessage(content, phoneNumbers));
        }
    }
    
}
