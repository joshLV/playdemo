package functional;

import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.mail.MailMessage;
import models.sales.Category;
import models.sales.TuanNoCategoryData;
import notifiers.MailSender;
import org.junit.Test;
import play.libs.Mail;
import play.test.FunctionalTest;

import java.util.LinkedList;
import java.util.List;

/**
 * User: wangjia
 * Date: 12-10-24
 * Time: 下午2:06
 */
public class TuanCategoryMailsTest extends FunctionalTest {

    @Test
    public void testDirectTuanMailSend() {
        String email = "826912022@qq.com";
        String mailUrl = "http://localhost:7001";
        MailMessage message = new MailMessage();
        String tuanName = "Tuan";
        List<TuanNoCategoryData> noTuanCategoryMessageList = new LinkedList<>();
        List<Category> categoryList = FactoryBoy.batchCreate(15, Category.class,
                new SequenceCallback<Category>() {
                    @Override
                    public void sequence(Category category, int seq) {
                        category.name = "Child #" + seq;
                    }
                });
        for (Category c : categoryList) {
            noTuanCategoryMessageList.add(TuanNoCategoryData.from(c));
        }
        message.setSubject(tuanName + "收录分类[测试]");
        message.addRecipient(email);
        message.putParam("mail_url", mailUrl);
        message.putParam("tuanName", tuanName);
        message.putParam("mailCategoryList", noTuanCategoryMessageList);
        message.setTemplate("tuanCategory");
        MailSender.send(message);

        String mailBody = Mail.Mock.getLastMessageReceivedBy(email);
        assertTrue(tuanName, mailBody.indexOf(tuanName) > 0);
    }

    @Test
    public void testTuanMailSendByMQ() throws Exception {
        String email = "826912022@qq.com";
        String mailUrl = "http://localhost:7001";
        MailMessage message = new MailMessage();
        String tuanName = "Tuan";
        List<TuanNoCategoryData> noTuanCategoryMessageList = new LinkedList<>();
        List<Category> categoryList = FactoryBoy.batchCreate(15, Category.class,
                new SequenceCallback<Category>() {
                    @Override
                    public void sequence(Category category, int seq) {
                        category.name = "Child #" + seq;
                    }
                });
        for (Category c : categoryList) {
            noTuanCategoryMessageList.add(TuanNoCategoryData.from(c));
        }
        message.setSubject(tuanName + "收录分类[测试]");
        message.addRecipient(email);
        message.putParam("mail_url", mailUrl);
        message.putParam("tuanName", tuanName);
        message.putParam("mailCategoryList", noTuanCategoryMessageList);

        message.setTemplate("tuanCategory");
        MailSender.send(message);
        Thread.sleep(500);

        String mailBody = Mail.Mock.getLastMessageReceivedBy(email);
        assertTrue(tuanName, mailBody.indexOf(tuanName) > 0);
    }


}
