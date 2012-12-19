package models.mail;

import play.Play;
import util.mq.MQPublisher;

public class MailUtil {
    public static final String TUAN_CATEGORY_NOTIFY = Play.mode.isProd() ? "tuan_notification" : "tuan_notification_dev";

    public static final String COMMON_QUEUE = Play.mode.isProd() ? "common_mail" : "common_mail_dev";

    private MailUtil() {
    }


    /**
     * 调用此函数前需要在message中指定template，就像下面那些函数做的一样
     */
    public static void sendCommonMail(MailMessage message) {
        MQPublisher.publish(COMMON_QUEUE, message);
    }

    public static void sendCouponMail(MailMessage message) {
        message.setSubject("[一百券] 您订购的消费券");
        message.setTemplate("couponMail");
        MQPublisher.publish(COMMON_QUEUE, message);
    }

    public static void sendFindPasswordMail(MailMessage message) {
        message.setSubject("[一百券] 找回密码");
        message.setTemplate("findPassword");
        MQPublisher.publish(COMMON_QUEUE, message);
    }

    public static void sendOperatorNotificationMail(MailMessage message) {
        MQPublisher.publish(COMMON_QUEUE, message);
    }

    public static void sendGoodsOffSalesMail(MailMessage message) {
        message.setTemplate("goodsOffSales");
        MQPublisher.publish(COMMON_QUEUE, message);
    }


    public static void sendTuanCategoryMail(MailMessage message) {
        message.setTemplate("tuanCategory");
        MQPublisher.publish(COMMON_QUEUE, message);
    }

    public static void sendCustomerRemarkMail(MailMessage message) {
        message.setTemplate("customerRemarkMail");
        MQPublisher.publish(COMMON_QUEUE, message);
    }

}
