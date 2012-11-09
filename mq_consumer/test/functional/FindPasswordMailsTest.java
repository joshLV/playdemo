package functional;


import models.mail.MailMessage;
import notifiers.FindPassWordMails;

import org.junit.Test;

import play.libs.Mail;
import play.test.FunctionalTest;

public class FindPasswordMailsTest extends FunctionalTest {

    @Test
    public void testDirectCouponMailSend() {
        String email = "115348712@qq.com";
        String mailUrl ="http://localhost:7001";
        MailMessage message = new MailMessage();
        message.addRecipient(email);
        message.putParam("mail_url", mailUrl);

        FindPassWordMails.notify(message);
        
        String mailBody = Mail.Mock.getLastMessageReceivedBy(email);
//        System.out.println(mailBody);
        assertTrue("邮件标题不正确", mailBody.indexOf("Subject: [一百券] 找回密码") > 0);
        assertTrue("邮件中必须出现url", mailBody.indexOf(mailUrl) > 0);
    }
    
    @Test
    public void testCouponMailSendByMQ() throws Exception {
        String email = "test2@uhuila.com";
        String mailUrl ="http://localhost:7001";
        MailMessage message = new MailMessage();
        message.addRecipient(email);
        message.putParam("mail_url", mailUrl);

        FindPassWordMails.notify(message);
        Thread.sleep(500);
        
        String mailBody = Mail.Mock.getLastMessageReceivedBy(email);
//        System.out.println(mailBody);
        assertTrue("邮件标题不正确", mailBody.indexOf("Subject: [一百券] 找回密码") > 0);
        assertTrue("邮件中必须出现url", mailBody.indexOf(mailUrl) > 0);
    }
}
