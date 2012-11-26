package unit;

import factory.FactoryBoy;

import factory.callback.SequenceCallback;
import models.mail.MailMessage;
import models.sales.Category;
import models.sales.Goods;
import models.sales.TuanNoCategoryData;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.libs.Mail;
import play.test.UnitTest;

import java.util.LinkedList;
import java.util.List;

import util.mq.*;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-11-26
 * Time: 下午3:59
 * To change this template use File | Settings | File Templates.
 */
public class TuanNoCategoryMessageMailTest extends UnitTest {
    List<Goods> goodsList;
    public static final String TUAN_CATEGORY_NOTIFY = Play.mode.isProd() ? "tuan_notification" : "tuan_notification_dev";

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        goodsList = FactoryBoy.batchCreate(50, Goods.class,
                new SequenceCallback<Goods>() {
                    @Override
                    public void sequence(Goods target, int seq) {
                        target.name = "Test#" + seq;
                    }
                });

    }

    @Test
    public void testTuanDirectMailSend() {
        Goods.filterTopGoods(goodsList, "tuan360test", "tuan360", 6);
        MailMessage mailBody = (MailMessage) MockMQ.getLastMessage(TUAN_CATEGORY_NOTIFY);
        assertNotNull(mailBody);
        assertTrue(mailBody.getRecipients().contains("dev@uhuila.com"));
        assertEquals("tuan360", mailBody.getParam("tuanName"));
    }

    @Test
    public void testTuanMailSendByMQ() throws Exception {
        Goods.filterTopGoods(goodsList, "tuan360test", "tuan360", 6);
        Thread.sleep(500);
        MailMessage mailBody = (MailMessage) MockMQ.getLastMessage(TUAN_CATEGORY_NOTIFY);
        assertNotNull(mailBody);
        assertTrue(mailBody.getRecipients().contains("dev@uhuila.com"));
        assertEquals("tuan360", mailBody.getParam("tuanName"));
    }
}
