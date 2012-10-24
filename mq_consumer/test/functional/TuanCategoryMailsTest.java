package functional;

import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.mail.MailMessage;
import models.sales.Category;
import notifiers.TuanCategoryMails;
import org.junit.Ignore;
import org.junit.Test;
import play.libs.Mail;
import play.test.FunctionalTest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-10-24
 * Time: 下午2:06
 * To change this template use File | Settings | File Templates.
 */
public class TuanCategoryMailsTest extends FunctionalTest {

    @Test
    public void testDirectCouponMailSend() {
        String email = "826912022@qq.com";
        String mailUrl = "http://localhost:7001";
        MailMessage message = new MailMessage();
        String tuanName = "Tuan";
        List<Category> noMessage = new ArrayList<>();
        List<Category> categoryList = FactoryBoy.batchCreate(15, Category.class,
                new SequenceCallback<Category>() {
                    @Override
                    public void sequence(Category category, int seq) {
                        category.name = "Child #" + seq;
                    }
                });
        for (Category c : categoryList) {
            noMessage.add(c);
        }
        message.setSubject(tuanName + "收录分类[测试]");
        message.addRecipient(email);
        message.putParam("mail_url", mailUrl);
        message.putParam("tuanName", tuanName);
        message.putParam("mailCategoryList", noMessage);
        TuanCategoryMails.notify(message);

        String mailBody = Mail.Mock.getLastMessageReceivedBy(email);
        System.out.println(mailBody);
        assertTrue(tuanName, mailBody.indexOf(tuanName) > 0);
    }

    @Test
    public void testCouponMailSendByMQ() throws Exception {
        String email = "826912022@qq.com";
        String mailUrl = "http://localhost:7001";
        MailMessage message = new MailMessage();
        String tuanName = "Tuan";
        List<Category> noMessage = new ArrayList<>();
        List<Category> categoryList = FactoryBoy.batchCreate(15, Category.class,
                new SequenceCallback<Category>() {
                    @Override
                    public void sequence(Category category, int seq) {
                        category.name = "Child #" + seq;
                    }
                });
        for (Category c : categoryList) {
            noMessage.add(c);
        }
        message.setSubject(tuanName + "收录分类[测试]");
        message.addRecipient(email);
        message.putParam("mail_url", mailUrl);
        message.putParam("tuanName", tuanName);
        message.putParam("mailCategoryList", noMessage);
        TuanCategoryMails.notify(message);
        Thread.sleep(500);

        String mailBody = Mail.Mock.getLastMessageReceivedBy(email);
        System.out.println(mailBody);
        assertTrue(tuanName, mailBody.indexOf(tuanName) > 0);
    }


}
