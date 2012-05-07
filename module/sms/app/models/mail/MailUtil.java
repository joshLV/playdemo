package models.mail;

import play.Play;
import play.modules.rabbitmq.producer.RabbitMQPublisher;

public class MailUtil {
	public static final String COUPON_MAIL_QUEUE_NAME = Play.mode.isProd() ? "coupon_mail" : 
	    (Play.runingInTestMode() ? "coupon_mail_" + System.currentTimeMillis() : "coupon_mail_dev");
	public static final String MAIL_QUEUE_NAME = Play.mode.isProd() ? "find_password_mail" : "find_password_mail_dev";
	private MailUtil(){}
	public static void send(MailMessage message){
		RabbitMQPublisher.publish(COUPON_MAIL_QUEUE_NAME, message);
	}

	public static void sendFindPasswordMail(MailMessage message){
		RabbitMQPublisher.publish(MAIL_QUEUE_NAME, message);
	}
}
