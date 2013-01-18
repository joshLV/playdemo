package unit;

import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.sales.Goods;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.mq.MockMQ;

import java.util.List;

/**
 * User: wangjia
 * Date: 12-11-26
 * Time: 下午3:59
 */
public class TuanNoCategoryMessageMailTest extends UnitTest {
    List<Goods> goodsList;

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

    @After
    public void tearDown() throws Exception {
        MockMQ.clear();
    }

    @Test
    public void testTuanDirectMailSend() {
        Goods.filterTopGoods(goodsList, "tuan360test", "tuan360", 6);
        MailMessage mailBody = (MailMessage) MockMQ.getLastMessage(MailUtil.COMMON_QUEUE);
        assertNotNull(mailBody);
        assertTrue(mailBody.getRecipients().contains("dev@uhuila.com"));
        assertEquals("tuan360", mailBody.getParam("tuanName"));
    }

    @Test
    public void testTuanMailSendByMQ() throws Exception {
        Goods.filterTopGoods(goodsList, "tuan360test", "tuan360", 6);
        Thread.sleep(500);
        MailMessage mailBody = (MailMessage) MockMQ.getLastMessage(MailUtil.COMMON_QUEUE);
        assertNotNull(mailBody);
        assertTrue(mailBody.getRecipients().contains("dev@uhuila.com"));
        assertEquals("tuan360", mailBody.getParam("tuanName"));
    }
}
