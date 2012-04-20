package models.mail;

import play.Play;
import play.modules.rabbitmq.producer.RabbitMQPublisher;

public class MailUtil {
    public static final String COUPON_MAIL_QUEUE_NAME = Play.mode.isProd() ? "coupon_mail" : "coupon_mail_dev";
    private MailUtil(){}
    public static void send(CouponMessage message){
        RabbitMQPublisher.publish(COUPON_MAIL_QUEUE_NAME, message);
    }
}
