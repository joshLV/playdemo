package models.mail;

import play.Play;
import util.mq.MQPublisher;

/**
 * 新建一个邮件模板的步骤：
 * 1. 在mq_consumer/app/views/MailSender中创建自己的模板
 * 2. 自己组装MailMessage，
 *     MailMessage message = new MailMessage();
 *     message.setSubject() 设置标题
 *     message.addRecipient() 设置收件人，参数为多个String或者一个String数组
 *     message.setTemplate() 设置模板，名字与上一步创建的模板名字相同
 *     message.putParam()  添加模板变量，可多次添加，变量可以在模板中以message.getParam()的方式使用
 * 3. 调用MailUtil.sendCommonMail发送邮件
 * 4. （不必须）可以在下面创建自己的static方法，代码请参考以下
 */
public class MailUtil {
    public static final String COMMON_QUEUE = Play.mode.isProd() ? "common_mail" : "common_mail_dev";

    private MailUtil() {
    }


    public static void sendCommonMail(MailMessage message) {
        MQPublisher.publish(COMMON_QUEUE, message);
    }

    public static void sendCouponMail(MailMessage message) {
        message.setSubject("[一百券] 您订购的消费券");
        message.setTemplate("couponMail");
        sendCommonMail(message);
    }

    public static void sendFindPasswordMail(MailMessage message) {
        message.setSubject("[一百券] 找回密码");
        message.setTemplate("findPassword");
        sendCommonMail(message);
    }

    public static void sendGoodsOffSalesMail(MailMessage message) {
        message.setTemplate("goodsOffSales");
        sendCommonMail(message);
    }

    public static void sendExpiredContractNoticeMail(MailMessage message) {
        message.setTemplate("expiredContract");
        sendCommonMail(message);
    }
     public static void sendExpiredNoRefundCouponMail(MailMessage message) {
        message.setTemplate("expiredNoRefundCoupon");
        sendCommonMail(message);
    }
    public static void sendTuanCategoryMail(MailMessage message) {
        message.setTemplate("tuanCategory");
        sendCommonMail(message);
    }

    public static void sendCustomerRemarkMail(MailMessage message) {
        message.setTemplate("customerRemarkMail");
        sendCommonMail(message);
    }

}
