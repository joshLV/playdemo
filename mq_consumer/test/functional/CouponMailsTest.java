package functional;

import java.util.ArrayList;
import models.mail.CouponMessage;
import models.mail.MailUtil;
import notifiers.CouponMails;
import org.junit.Test;
import play.libs.Mail;
import play.test.FunctionalTest;

public class CouponMailsTest extends FunctionalTest {

    @Test
    public void testDirectCouponMailSend() {
        String email = "test@uhuila.com";
        String fullName = "张三";
        String coupon1 = "0280ec55a4fb4dde3aef27ee287d61c9";
        String coupon2 = "2720737f2d56c6f50528fc3307d5fa91";
        CouponMessage message = new CouponMessage();
        message.setEmail(email);
        message.setFullName(fullName);
        ArrayList<String> coupons = new ArrayList<String>();
        coupons.add(coupon1);
        coupons.add(coupon2);
        message.setCoupons(coupons);
        
        CouponMails.notify(message);
        
        String mailBody = Mail.Mock.getLastMessageReceivedBy(email);
        System.out.println(mailBody);
        assertTrue("邮件标题不正确", mailBody.indexOf("Subject: [优惠啦] 您订购优惠券") > 0);
        assertTrue("邮件中必须出现fullName", mailBody.indexOf(fullName) > 0);
        assertTrue("邮件中必须出现fulcoupon1", mailBody.indexOf(coupon1) > 0);
        assertTrue("邮件中必须出现fulcoupon2", mailBody.indexOf(coupon2) > 0);
    }
    
    @Test
    public void testCouponMailSendByMQ() throws Exception {
        String email = "test3@uhuila.com";
        String fullName = "李四";
        String coupon1 = "9780ec55a4fb4ddeabef27ee287d61c9";
        String coupon2 = "0420737f2d56c1150528fc3307d5fa91";
        CouponMessage message = new CouponMessage();
        message.setEmail(email);
        message.setFullName(fullName);
        ArrayList<String> coupons = new ArrayList<String>();
        coupons.add(coupon1);
        coupons.add(coupon2);
        message.setCoupons(coupons);
        
        MailUtil.send(message);
        Thread.sleep(500);
        
        String mailBody = Mail.Mock.getLastMessageReceivedBy(email);
        System.out.println(mailBody);
        assertTrue("邮件标题不正确", mailBody.indexOf("Subject: [优惠啦] 您订购优惠券") > 0);
        assertTrue("邮件中必须出现fullName", mailBody.indexOf(fullName) > 0);
        assertTrue("邮件中必须出现fulcoupon1", mailBody.indexOf(coupon1) > 0);
        assertTrue("邮件中必须出现fulcoupon2", mailBody.indexOf(coupon2) > 0);
    }
}
