package unit;

import com.uhuila.common.util.DateUtil;
import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.consumer.User;
import models.order.CouponHistory;
import models.order.CouponsCondition;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderStatus;
import models.order.OrdersCondition;
import models.order.PromoteRebate;
import models.order.RebateStatus;
import models.resale.Resaler;
import models.sms.SMSMessage;
import models.sms.SMSUtil;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.test.UnitTest;
import util.DateHelper;
import util.mq.MockMQ;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;


public class ECouponUnitTest extends UnitTest {
    User user;
    Order order;
    ECoupon eCoupon;
    CouponHistory couponHistory;
    Resaler resaler;

    @Before
    public void loadData() {
        FactoryBoy.deleteAll();
        user = FactoryBoy.create(User.class);
        order = FactoryBoy.create(Order.class);
        order.setUser(user.id, AccountType.CONSUMER);
        order.promoteUserId = user.id;
        order.paidAt = new Date();
        order.save();
        eCoupon = FactoryBoy.create(ECoupon.class);
        resaler = FactoryBoy.create(Resaler.class);
    }

    /**
     * 测试会员中心订单列表
     */
    @Test
    public void testOrder() throws ParseException {
        OrdersCondition condition = new OrdersCondition();
        condition.createdAtBegin = DateHelper.beforeDays(new Date(), 1);
        condition.createdAtEnd = new Date();
        condition.status = OrderStatus.UNPAID;
        condition.goodsName = "Product Name";
        int pageNumber = 1;
        int pageSize = 15;
        JPAExtPaginator<Order> list = Order.findUserOrders(user, condition, pageNumber, pageSize);
        assertEquals(1, list.size());

        list = Order.findUserOrders(null, condition, pageNumber, pageSize);
        assertEquals(0, list.size());
    }

    /**
     * 测试会员中心券列表
     */
    @Test
    public void testQueryCoupons() {

        eCoupon.goods.isLottery = false;
        eCoupon.consumedAt = DateHelper.beforeDays(3);
        eCoupon.save();
        CouponsCondition condition = new CouponsCondition();
        condition.consumedAtBegin = DateHelper.beforeDays(4);
        condition.goodsName = "Product Name";
        JPAExtPaginator<ECoupon> list = ECoupon.query(condition, 1, 15);
        assertEquals(1, list.size());

    }

    @Test
    public void testUnFreeze() {
        assertEquals(new Integer(0), eCoupon.isFreeze);
        ECoupon.freeze(eCoupon.id, "tom");
        assertEquals(new Integer(1), eCoupon.isFreeze);
        couponHistory = CouponHistory.find("coupon=? order by createdAt desc", eCoupon).first();
        assertEquals("冻结券号", couponHistory.remark);
        assertEquals("tom", couponHistory.operator);

        ECoupon.unfreeze(eCoupon.id, "jan");
        assertEquals(new Integer(0), eCoupon.isFreeze);
        couponHistory.refresh();
        couponHistory = CouponHistory.find("coupon=? order by createdAt desc", eCoupon).first();
        assertEquals("解冻券号", couponHistory.remark);
        assertEquals("jan", couponHistory.operator);
    }

    @Test
    public void testSendUserMessage_不包含门店的信息发送() {
        assertTrue(ECoupon.sendUserMessage(eCoupon.id, "13712345678"));
        SMSMessage checkMsg = (SMSMessage) MockMQ.getLastMessage(SMSUtil.SMS_QUEUE);
        assertEquals("【一百券】" + eCoupon.goods.title + "券号" + eCoupon.eCouponSn + ",截止" + DateUtil.dateToString(eCoupon.goods.expireAt, 0) + ",客服：4006262166", checkMsg.getContent());
    }

    @Test
    public void testSendUserMessageInfoWithoutCheck_包含门店的信息发送() {
        eCoupon.shop = eCoupon.goods.getShopList().iterator().next();
        eCoupon.save();
        ECoupon.sendUserMessageInfoWithoutCheck("13712345678", eCoupon, eCoupon.shop.id.toString());
        SMSMessage checkMsg = (SMSMessage) MockMQ.getLastMessage(SMSUtil.SMS_QUEUE);
        assertEquals("【一百券】" + eCoupon.goods.title + "券号" + eCoupon.eCouponSn + ",截止" + DateUtil.dateToString(eCoupon.goods.expireAt, 0) + ",[" +
                eCoupon.shop.name + "]" + eCoupon.shop.address + " " + eCoupon.shop.phone + ";客服：4006262166", checkMsg.getContent());
    }

    @Test
    public void testSendMessage_运营后台重发短信() {
        assertTrue(ECoupon.sendMessage(eCoupon.id));
        SMSMessage checkMsg = (SMSMessage) MockMQ.getLastMessage(SMSUtil.SMS_QUEUE);
        assertEquals("【一百券】" + eCoupon.goods.title + "券号" + eCoupon.eCouponSn + ",截止" + DateUtil.dateToString(eCoupon.goods.expireAt, 0) + ",客服：4006262166", checkMsg.getContent());

    }

    /**
     * 测试退款
     */
    @Test
    public void applyRefund() {
        String ret = ECoupon.applyRefund(null, user.id, AccountType.CONSUMER);
        assertEquals("{\"error\":\"no such eCoupon\"}", ret);
        eCoupon.order.userId = user.id;
        eCoupon.save();
        Account account = AccountUtil.getPlatformIncomingAccount();
        account.amount = new BigDecimal("1000000");
        account.save();

        eCoupon.refresh();
        eCoupon.status = ECouponStatus.CONSUMED;
        eCoupon.save();
        ret = ECoupon.applyRefund(eCoupon, user.id, AccountType.CONSUMER);
        assertEquals("{\"error\":\"can not apply refund with this goods\"}", ret);

        eCoupon.status = ECouponStatus.UNCONSUMED;
        eCoupon.save();
        eCoupon.refresh();
        assertEquals(new BigDecimal("0.00"), eCoupon.refundPrice);
        assertNull(eCoupon.refundAt);

        eCoupon.order.accountPay = new BigDecimal("8.50");
        eCoupon.order.save();
        long cnt = CouponHistory.count();
        assertEquals(cnt, CouponHistory.count());
        ret = ECoupon.applyRefund(eCoupon, user.id, AccountType.CONSUMER);
        assertEquals("{\"error\":\"ok\"}", ret);
        assertEquals(ECouponStatus.REFUND, eCoupon.status);
        assertEquals(new BigDecimal("8.50"), eCoupon.refundPrice);
        assertNotNull(eCoupon.refundAt);

        assertEquals(cnt + 1, CouponHistory.count());
        couponHistory = CouponHistory.find("coupon=?", eCoupon).first();
        assertEquals("券退款", couponHistory.remark);
        assertEquals("消费者:" + user.getShowName(), couponHistory.operator);

        eCoupon.refresh();
        eCoupon.status = ECouponStatus.UNCONSUMED;
        eCoupon.order.userId = resaler.id;
        eCoupon.order.userType = AccountType.RESALER;
        eCoupon.refundPrice = BigDecimal.ZERO;
        eCoupon.order.refundedAmount = BigDecimal.ZERO;
        eCoupon.order.save();
        eCoupon.save();
        assertEquals(BigDecimal.ZERO, eCoupon.refundPrice);
        ret = ECoupon.applyRefund(eCoupon, resaler.id, AccountType.RESALER);
        assertEquals("{\"error\":\"ok\"}", ret);
        assertEquals(ECouponStatus.REFUND, eCoupon.status);
        assertEquals(new BigDecimal("8.50"), eCoupon.refundPrice);
        couponHistory.refresh();
        couponHistory = CouponHistory.find("coupon=? order by createdAt desc", eCoupon).first();
        assertEquals("券退款", couponHistory.remark);
        assertEquals("分销商:" + resaler.loginName, couponHistory.operator);
    }

    @Test
    public void getEcouponSn() {
        String sn = eCoupon.getMaskedEcouponSn();
        assertEquals("******" + sn.substring(6), sn);

    }

    @Test
    public void getConsumedPromoteRebateAmount() {
        User user1 = FactoryBoy.create(User.class);
        PromoteRebate promoteRebate = FactoryBoy.create(PromoteRebate.class);
        promoteRebate.promoteUser = user;
        promoteRebate.invitedUser = user1;
        promoteRebate.order = order;
        promoteRebate.status = RebateStatus.ALREADY_REBATE;
        promoteRebate.save();
        eCoupon.status = ECouponStatus.CONSUMED;
        eCoupon.save();

        BigDecimal money = ECoupon.getConsumedPromoteRebateAmount(user.id);
        assertEquals(new BigDecimal("8.50"), money);
    }

    @Test
    public void testGetSavedMoney() {
        BigDecimal savedMoney = ECoupon.getSavedMoney(user.id, AccountType.CONSUMER);
        assertEquals(new BigDecimal("1.50"), savedMoney);
    }
}
