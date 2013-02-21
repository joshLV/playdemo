package models.sms;

import play.Play;
import util.mq.MQPublisher;

import java.util.Arrays;
import java.util.List;

/**
 * User: likang
 */
public class SMSUtil {

    // 短信MQ名称
    public static final String SMS_QUEUE = Play.mode.isProd() ? "send_sms" : "send_sms_dev";

    // 短信MQ名称，用于第二通道.
    public static final String SMS2_QUEUE = Play.mode.isProd() ? "send_sms2" : "send_sms2_dev";

    public static void send(String content, String phoneNumber, String code){
        MQPublisher.publish(SMS_QUEUE, new SMSMessage(content+"【一百券】", phoneNumber, code));
    }

    public static void send(String content, String phoneNumber){
        MQPublisher.publish(SMS_QUEUE, new SMSMessage(content+"【一百券】", phoneNumber));
    }

    public static void send(String content, String[] phoneNumbers){
        MQPublisher.publish(SMS_QUEUE, new SMSMessage(content+"【一百券】", Arrays.asList(phoneNumbers)));
    }

    public static void send2(String content, String phoneNumber, String code){
        MQPublisher.publish(SMS2_QUEUE, new SMSMessage(content+"【一百券】", phoneNumber, code));
    }
    public static void send2(String content, List<String> phoneNumbers){
        MQPublisher.publish(SMS2_QUEUE, new SMSMessage(content+"【一百券】", phoneNumbers));
    }

}
