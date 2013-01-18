package unit;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.job.ExpiredCouponNotice;
import models.order.ECoupon;
import models.order.SentCouponMessage;
import models.sales.Goods;
import models.sms.SMSMessage;
import models.sms.SMSUtil;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.DateHelper;
import util.mq.MockMQ;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * TODO.
 * <p/>
 * User: yanjy
 * Date: 12-5-31
 * Time: 下午3:09
 */
public class ExpiredCouponUnitTest extends UnitTest {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    
    @Before
    public void setup() {
        FactoryBoy.deleteAll();
    }

    @Test
    public void testJob() throws Exception {
        Goods goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.expireAt = DateHelper.afterDays(7);
            }
            
        });
        FactoryBoy.create(ECoupon.class);
  
        ExpiredCouponNotice job = new ExpiredCouponNotice();
        job.doJob();

        List<SentCouponMessage> sentList = SentCouponMessage.findAll();
        assertEquals(1, sentList.size());

        SMSMessage msg = (SMSMessage)MockMQ.getLastMessage(SMSUtil.SMS_QUEUE);
        assertEquals("【一百券】您的" + goods.name + "，将要过期，请注意消费截止日期为" + sdf.format(goods.expireAt) + "。", msg.getContent());

        job.doJob();

        // 不会再发短信.
        assertEquals(0, MockMQ.size(SMSUtil.SMS_QUEUE));
        sentList = SentCouponMessage.findAll();
        assertEquals(1, sentList.size());

    }

    @Test
    public void testJobWhenNoNeedSendCouponIn8() throws Exception {
        FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.expireAt = DateHelper.afterDays(8);
            }
            
        });
        FactoryBoy.create(ECoupon.class);
  
        ExpiredCouponNotice job = new ExpiredCouponNotice();
        job.doJob();

        List<SentCouponMessage> sentList = SentCouponMessage.findAll();
        assertEquals(0, sentList.size());

        // 不会再发短信.
        assertEquals(0, MockMQ.size(SMSUtil.SMS_QUEUE));
    }
    
    @Test
    public void testJobWhenNoNeedSendCouponIn6() throws Exception {
        FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.expireAt = DateHelper.afterDays(6);
            }
            
        });
        FactoryBoy.create(ECoupon.class);
  
        ExpiredCouponNotice job = new ExpiredCouponNotice();
        job.doJob();

        List<SentCouponMessage> sentList = SentCouponMessage.findAll();
        assertEquals(0, sentList.size());

        // 不会再发短信.
        assertEquals(0, MockMQ.size(SMSUtil.SMS_QUEUE));
    }
}
