package models.mail;

import play.Play;
import play.modules.rabbitmq.producer.RabbitMQPublisher;
import util.mq.*;

public class MailUtil {
    public static final String COUPON_MAIL_QUEUE_NAME = Play.mode.isProd() ? "coupon_mail" :
            (Play.runingInTestMode() ? "coupon_mail_" + System.currentTimeMillis() : "coupon_mail_dev");
    public static final String FIND_PWD_MAIL_QUEUE_NAME = Play.mode.isProd() ? "find_password_mail" : "find_password_mail_dev";
    public static final String OPERATOR_NOTIFICATION = Play.mode.isProd() ? "operator_notification" : "operator_notification_dev";
    public static final String GOODS_OFF_SALES_NOTIFY = Play.mode.isProd() ? "goods_off_sales_notify" : "goods_off_sales_dev";
    public static final String FINANCE_NOTIFICATION = Play.mode.isProd() ? "finance_notification" : "finance_notification_dev";
    public static final String TUAN_CATEGORY_NOTIFY = Play.mode.isProd() ? "tuan_notification" : "tuan_notification_dev";
    public static final String CUSTOMER_REMARK_NOTIFY = Play.mode.isProd() ? "customer_remark_notification" : "customer_remark_notification_dev";

    private MailUtil() {
    }

    public static void sendCouponMail(MailMessage message) {
        MQPublisher.publish(COUPON_MAIL_QUEUE_NAME, message);
    }

    public static void sendFindPasswordMail(MailMessage message) {
        MQPublisher.publish(FIND_PWD_MAIL_QUEUE_NAME, message);
    }

    public static void sendOperatorNotificationMail(MailMessage message) {
        MQPublisher.publish(OPERATOR_NOTIFICATION, message);
    }

    public static void sendGoodsOffSalesMail(MailMessage message) {
        MQPublisher.publish(GOODS_OFF_SALES_NOTIFY, message);
    }

    public static void sendFinanceNotificationMail(MailMessage message) {
        MQPublisher.publish(FINANCE_NOTIFICATION, message);
    }

    public static void sendMail(MailMessage message) {
        MQPublisher.publish(GOODS_OFF_SALES_NOTIFY, message);

    }

    public static void sendTuanCategoryMail(MailMessage message) {
        MQPublisher.publish(TUAN_CATEGORY_NOTIFY, message);
    }

    public static void sendCustomerRemarkMail(MailMessage message) {
        MQPublisher.publish(CUSTOMER_REMARK_NOTIFY, message);
    }


}
