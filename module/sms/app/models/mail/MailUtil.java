package models.mail;

import play.modules.rabbitmq.producer.RabbitMQPublisher;

public class MailUtil {
    private static final String COUPON_MAIL_QUEUE_NAME = "coupon_mail";
    private MailUtil(){}
    public static void send(CouponMessage message){
        RabbitMQPublisher.publish(COUPON_MAIL_QUEUE_NAME, message);
    }
}
