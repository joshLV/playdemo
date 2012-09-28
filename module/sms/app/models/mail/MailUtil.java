package models.mail;

import play.Play;
import play.modules.rabbitmq.producer.RabbitMQPublisher;

public class MailUtil {
    public static final String COUPON_MAIL_QUEUE_NAME = Play.mode.isProd() ? "coupon_mail" :
            (Play.runingInTestMode() ? "coupon_mail_" + System.currentTimeMillis() : "coupon_mail_dev");
    public static final String FIND_PWD_MAIL_QUEUE_NAME = Play.mode.isProd() ? "find_password_mail" : "find_password_mail_dev";
    public static final String OPERATOR_NOTIFICATION = Play.mode.isProd() ? "operator_notification" : "operator_notification_dev";
    public static final String GOODS_OFF_SALES_NOTIFY = Play.mode.isProd() ? "goods_off_sales_notify" : "goods_off_sales_dev";
    public static final String FINANCE_NOTIFICATION = Play.mode.isProd() ? "finance_notification" : "finance_notification_dev";

    private MailUtil() {
    }

    public static void sendCouponMail(MailMessage message) {
        RabbitMQPublisher.publish(COUPON_MAIL_QUEUE_NAME, message);
    }

    public static void sendFindPasswordMail(MailMessage message) {
        RabbitMQPublisher.publish(FIND_PWD_MAIL_QUEUE_NAME, message);
    }

    public static void sendOperatorNotificationMail(MailMessage message) {
        RabbitMQPublisher.publish(OPERATOR_NOTIFICATION, message);
    }

    public static void sendGoodsOffSalesMail(MailMessage message) {
        RabbitMQPublisher.publish(GOODS_OFF_SALES_NOTIFY, message);
    }

    public static void sendFinanceNotificationMail(MailMessage message) {
        RabbitMQPublisher.publish(FINANCE_NOTIFICATION, message);
    }

    public static void sendMail(MailMessage message) {
        RabbitMQPublisher.publish(GOODS_OFF_SALES_NOTIFY, message);
    }

}
