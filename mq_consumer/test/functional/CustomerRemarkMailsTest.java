package functional;

import factory.FactoryBoy;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.order.Order;
import notifiers.MailSender;
import org.junit.Before;
import org.junit.Test;
import play.libs.Mail;
import play.test.FunctionalTest;

/**
 * User: wangjia
 * Date: 12-10-24
 * Time: 下午1:27
 */
public class CustomerRemarkMailsTest extends FunctionalTest {
    Order order;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        order = FactoryBoy.create(Order.class);

    }

    @Test
    public void testDirectCouponMailSend() {
        String email = "826912022@qq.com";
        String mailUrl = "http://localhost:7001";
        MailMessage message = new MailMessage();
        message.addRecipient(email);
        message.putParam("mail_url", mailUrl);
        message.putParam("orderNumber", order.orderNumber);
        message.putParam("remark", order.remark);
        message.putParam("goodsName", "test " + "test1");
        message.putParam("phone", order.orderNumber);
        message.putParam("orderId", order.id);
        message.putParam("addr", play.Play.configuration.getProperty("application.baseUrl"));

        message.setTemplate("customerRemarkMail");
        MailSender.send(message);

        String mailBody = Mail.Mock.getLastMessageReceivedBy(email);
        assertTrue(order.orderNumber, mailBody.indexOf(order.orderNumber) > 0);
    }


    @Test
    public void testCouponMailSendByMQ() throws Exception {
        String email = "826912022@qq.com";
        String mailUrl = "http://localhost:7001";
        MailMessage message = new MailMessage();
        message.addRecipient(email);
        message.putParam("mail_url", mailUrl);
        message.putParam("orderNumber", order.orderNumber);
        message.putParam("remark", order.remark);
        message.putParam("goodsName", "test " + "test1");
        message.putParam("phone", order.orderNumber);
        message.putParam("orderId", order.id);
        message.putParam("addr", play.Play.configuration.getProperty("application.baseUrl"));

        message.setTemplate("customerRemarkMail");
        MailSender.send(message);
        Thread.sleep(500);
        String mailBody = Mail.Mock.getLastMessageReceivedBy(email);

        assertTrue(order.orderNumber, mailBody.indexOf(order.orderNumber) > 0);
    }
}
