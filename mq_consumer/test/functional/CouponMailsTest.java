package functional;

import models.mail.MailMessage;
import models.mail.MailUtil;
import notifiers.CouponMails;
import org.junit.Test;
import play.libs.Mail;
import play.modules.rabbitmq.RabbitMQPlugin;
import play.test.FunctionalTest;

import java.util.ArrayList;

public class CouponMailsTest extends FunctionalTest {

    @Test
    public void testDirectCouponMailSend() {
        Long time = System.currentTimeMillis() + 1;
        String email = "test" + time + "@uhuila.com";
        // String fullName = "张三";
        String fullName = "您已成功购买faasfaf\r订单号是3423543543，支付金额是+46456457元\r";

        String coupon1 = "0280ec55a4fb4dde3aef27ee287d61c9";
        String coupon2 = "2720737f2d56c6f50528fc3307d5fa91";
        MailMessage message = new MailMessage();
        message.addRecipient(email);
        message.putParam("full_name", fullName);
        ArrayList<String> coupons = new ArrayList<String>();
        coupons.add(coupon1);
        coupons.add(coupon2);

        CouponMails.notify(message);

        String mailBody = Mail.Mock.getLastMessageReceivedBy(email);
        System.out.println(">>>>>>>>>>>>>>>>>>>." + mailBody);
        assertTrue("邮件标题不正确", mailBody.indexOf("Subject: [一百券] 您订购的消费券") > 0);
        assertTrue("邮件中必须出现fullName", mailBody.indexOf(fullName) > 0);
//        assertTrue("邮件中必须出现fulcoupon1", mailBody.indexOf(coupon1) > 0);
//        assertTrue("邮件中必须出现fulcoupon2", mailBody.indexOf(coupon2) > 0);
    }

    @Test
    public void testCouponMailSendByMQ() throws Exception {
        Long time = System.currentTimeMillis() + 2;
        String email = "test" + time + "@uhuila.com";
        String fullName = "您已成功购买faasfaf,订单号是3423543543，支付金额是+46456457元。\r";
        String coupon1 = "9780ec55a4fb4ddeabef27ee287d61c9";
        String coupon2 = "0420737f2d56c1150528fc3307d5fa91";
        MailMessage message = new MailMessage();
        message.addRecipient(email);
        message.putParam("full_name", fullName);
        ArrayList<String> coupons = new ArrayList<String>();
        coupons.add(coupon1);
        coupons.add(coupon2);

        MailUtil.sendCouponMail(message);
        Thread.sleep(500);

        RabbitMQPlugin.retries();

        String mailBody = Mail.Mock.getLastMessageReceivedBy(email);
        System.out.println(mailBody);
        assertTrue("邮件标题不正确", mailBody.indexOf("Subject: [一百券] 您订购的消费券") > 0);
        assertTrue("邮件中必须出现fullName", mailBody.indexOf(fullName) > 0);
//        assertTrue("邮件中必须出现fulcoupon1", mailBody.indexOf(coupon1) > 0);
//        assertTrue("邮件中必须出现fulcoupon2", mailBody.indexOf(coupon2) > 0);
    }
}
