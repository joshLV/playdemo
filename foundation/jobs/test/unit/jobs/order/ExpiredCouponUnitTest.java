package unit.jobs.order;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import jobs.order.ExpiredCouponNotice;
import jobs.order.ExpiredNoRefundCouponNotice;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.order.ECoupon;
import models.order.ECouponPartner;
import models.order.SentCouponMessage;
import models.resale.Resaler;
import models.sales.Goods;
import models.sms.SMSMessage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.Logger;
import play.test.UnitTest;
import util.DateHelper;
import util.mq.MockMQ;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
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
    public void testNoRefundCouponJob_一个分销商相同产品产生多个券号的测试() throws Exception {
        final Goods goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.noRefund = true;
                g.isLottery = false;
            }

        });

              ECoupon coupon1 = FactoryBoy.create(ECoupon.class);
        coupon1.partner = ECouponPartner.WB;
        coupon1.expireAt = DateHelper.afterDays(3);
        coupon1.goods = goods;
        coupon1.save();

        ECoupon coupon = FactoryBoy.create(ECoupon.class);
        coupon.partner = ECouponPartner.WB;
        coupon.expireAt = DateHelper.afterDays(3);
        coupon.goods = goods;
        coupon.save();

        ECoupon coupon2 = FactoryBoy.create(ECoupon.class);
        coupon2.partner = ECouponPartner.TB;
        coupon2.expireAt = DateHelper.afterDays(3);
        coupon2.goods = goods;
        coupon2.save();

        ExpiredNoRefundCouponNotice job = new ExpiredNoRefundCouponNotice();
        job.doJob();
        MailMessage msg = (MailMessage) MockMQ.getLastMessage(MailUtil.COMMON_QUEUE);
        assertTrue(msg.getSubject().contains("虚拟验证券到期提醒"));
        List<Map<String, String>> couponList = (ArrayList) msg.getParam("couponList");
        assertEquals(1, couponList.size());
        assertEquals(coupon1.eCouponSn + "," + coupon.eCouponSn, couponList.get(0).get("p_couponSn"));

    }

    @Test
    @Ignore
    public void testNoRefundCouponJob() throws Exception {
        Goods goods0 = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.noRefund = true;
                g.isLottery = false;
            }

        });
        Goods goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.noRefund = true;
                g.isLottery = false;
            }

        });
        ECoupon coupon0 = FactoryBoy.create(ECoupon.class);
        coupon0.partner = ECouponPartner.JD;
        coupon0.expireAt = DateHelper.afterDays(3);
        coupon0.goods = goods;
        coupon0.save();
        ECoupon coupon1 = FactoryBoy.create(ECoupon.class);
        coupon1.partner = ECouponPartner.WB;
        coupon1.expireAt = DateHelper.afterDays(3);
        coupon1.goods = goods0;
        coupon1.save();

        ECoupon coupon = FactoryBoy.create(ECoupon.class);
        coupon.partner = ECouponPartner.WB;
        coupon.expireAt = DateHelper.afterDays(3);
        coupon.goods = goods;
        coupon.save();

        ExpiredNoRefundCouponNotice job = new ExpiredNoRefundCouponNotice();
        job.doJob();
        MailMessage msg = (MailMessage) MockMQ.getLastMessage(MailUtil.COMMON_QUEUE);
        assertTrue(msg.getSubject().contains("虚拟验证券到期提醒"));
        List<Map<String, String>> couponList = (ArrayList) msg.getParam("couponList");
        assertEquals(3, couponList.size());
        assertEquals(coupon.eCouponSn, couponList.get(2).get("p_couponSn"));
        assertEquals(coupon0.eCouponSn, couponList.get(0).get("p_couponSn"));
        assertEquals(coupon1.eCouponSn, couponList.get(1).get("p_couponSn"));
    }

    @Ignore
    @Test
    public void testJob() throws Exception {
        Goods goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.expireAt = DateHelper.afterDays(6);
            }

        });


        ECoupon coupon = FactoryBoy.create(ECoupon.class);
        coupon.goods = goods;
        coupon.expireAt = DateHelper.afterDays(6);
        coupon.save();

        Logger.info("yibaiquan.id=" + Resaler.getYibaiquan().id);
        Logger.info("order.userId=" + coupon.order.userId);
        coupon.order.userId = FactoryBoy.create(Resaler.class).id; //不是一百券的订单才会发短信

        Logger.info("testJob1");
        ExpiredCouponNotice job = new ExpiredCouponNotice();
        job.doJob();

        List<SentCouponMessage> sentList = SentCouponMessage.findAll();
        assertEquals(1, sentList.size());

        Logger.info("testJob2");
        SMSMessage msg = (SMSMessage) MockMQ.getLastMessage(SMSMessage.SMS_QUEUE);
        assertEquals("您的" + goods.name + "，将要过期，请注意消费截止日期为" + sdf.format(coupon.expireAt) + "。【一百券】", msg.getContent());

        job.doJob();

        // 不会再发短信.
//        assertEquals(0, MockMQ.size(SMSMessage.SMS_QUEUE));
        sentList = SentCouponMessage.findAll();
        assertEquals(1, sentList.size());

    }

    @Ignore
    @Test
    public void testJobWhenNoNeedSendCouponIn8() throws Exception {

        Goods goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.expireAt = DateHelper.afterDays(8);
            }

        });
        ECoupon coupon = FactoryBoy.create(ECoupon.class);
        coupon.goods = goods;
        coupon.expireAt = DateHelper.afterDays(8);
        coupon.save();
        ExpiredCouponNotice job = new ExpiredCouponNotice();
        job.doJob();

        List<SentCouponMessage> sentList = SentCouponMessage.findAll();
        assertEquals(0, sentList.size());

        // 不会再发短信.
//        assertEquals(0, MockMQ.size(SMSMessage.SMS_QUEUE));
    }

    @Ignore
    @Test
    public void testJobWhenNoNeedSendCouponIn6() throws Exception {
        Goods goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.expireAt = DateHelper.afterDays(5);
            }

        });
        ECoupon coupon = FactoryBoy.create(ECoupon.class);
        coupon.goods = goods;
        coupon.expireAt = DateHelper.afterDays(5);
        coupon.save();

        ExpiredCouponNotice job = new ExpiredCouponNotice();
        job.doJob();

        List<SentCouponMessage> sentList = SentCouponMessage.findAll();
        assertEquals(0, sentList.size());

        // 不会再发短信.
//        assertEquals(0, MockMQ.size(SMSMessage.SMS_QUEUE));
    }
}
