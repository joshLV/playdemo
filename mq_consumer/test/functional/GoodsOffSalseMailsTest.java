package functional;

import models.mail.MailMessage;
import notifiers.GoodsOffSalesMails;
import org.junit.Test;
import play.libs.Mail;
import play.test.FunctionalTest;

import java.util.Date;

public class GoodsOffSalseMailsTest extends FunctionalTest {

    @Test
    public void testMailSend() {
        Long time = System.currentTimeMillis() + 1;
        String email = "test" + time + "@uhuila.com";

        MailMessage mailMessage = new MailMessage();
        mailMessage.addRecipient(email);

        mailMessage.setSubject("商品下架");

        mailMessage.putParam("date", new Date());
        mailMessage.putParam("supplierName", "KFC");
        mailMessage.putParam("goodsName", "汉堡");
        mailMessage.putParam("faceValue", "10");
        mailMessage.putParam("operateUserName", "小小");

        GoodsOffSalesMails.notify(mailMessage);

        String mailBody = Mail.Mock.getLastMessageReceivedBy(email);
        assertTrue("商品下架", mailBody.indexOf("商品下架") > 0);
        assertTrue("汉堡", mailBody.indexOf("汉堡") > 0);
        assertTrue("小小", mailBody.indexOf("小小") > 0);
    }

    @Test
    public void testGoodsNotEnough() {
        Long time = System.currentTimeMillis() + 1;
        String email = "test" + time + "@uhuila.com";

        MailMessage mailMessage = new MailMessage();
        mailMessage.addRecipient(email);

        mailMessage.setSubject("库存不足，商品即将下架");
        mailMessage.putParam("date", new Date());
        mailMessage.putParam("supplierName", "KFC");
        mailMessage.putParam("goodsName", "汉堡");
        mailMessage.putParam("faceValue", "10");
        mailMessage.putParam("baseSales", "0");
        mailMessage.putParam("offSalesFlag", "noInventory");
        GoodsOffSalesMails.notify(mailMessage);

        String mailBody = Mail.Mock.getLastMessageReceivedBy(email);
        assertTrue("库存不足，商品即将下架", mailBody.indexOf("库存不足，商品即将下架") > 0);
        assertTrue("汉堡", mailBody.indexOf("汉堡") > 0);
    }
}
