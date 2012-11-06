package unit;

import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.AccountType;
import models.consumer.User;
import models.order.*;
import models.sales.Area;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.test.Fixtures;
import play.test.UnitTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CouponsUnitTest extends UnitTest {
    @Before
    public void setup() {
        FactoryBoy.delete(CouponHistory.class);
        Fixtures.delete(Category.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(Area.class);
        Fixtures.delete(Order.class);
        Fixtures.delete(OrderItems.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(User.class);
        Fixtures.delete(ECoupon.class);
        Fixtures.delete(Account.class);
        Fixtures.loadModels(
                "fixture/goods_base.yml",
                "fixture/user.yml",
                "fixture/goods.yml",
                "fixture/accounts.yml",
                "fixture/orders.yml",
                "fixture/orderItems.yml");
    }


    /**
     * 测试券列表
     */
    @Test
    public void testQueryCoupons() {
        Long supplierId = 1l;
        int pageNumber = 1;
        int pageSize = 15;
        List<ECoupon> list = ECoupon.queryCoupons(supplierId, pageNumber, pageSize);
        assertEquals(4, list.size());

    }

    /**
     * 测试用户中心券列表
     */
    @Test
    public void testUserQueryCoupons() throws ParseException {
        CouponsCondition condition = new CouponsCondition();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        condition.createdAtBegin = sdf.parse("2012-03-01");
        condition.createdAtEnd = new Date();
        condition.status = ECouponStatus.UNCONSUMED;
        condition.goodsName = "";
        Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
        condition.userId = userId;
        condition.accountType = AccountType.CONSUMER;
        int pageNumber = 1;
        int pageSize = 15;
        JPAExtPaginator<ECoupon> list = ECoupon.query(condition, pageNumber, pageSize, null, true);
        assertEquals(0, list.size());
    }

    /**
     * 测试用户中心券列表
     */
    @Test
    public void testECoupon() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        Goods goods = Goods.findById(goodsId);
        Long orderId = (Long) Fixtures.idCache.get("models.order.Order-order1");
        Order order = Order.findById(orderId);
        ECoupon coupon = null;
        for (OrderItems orderItem : order.orderItems) {
            coupon = new ECoupon(order, goods, orderItem);
            assertNotNull(coupon);
            assertNotNull(coupon.replyCode);
            assertEquals(4, coupon.replyCode.length());
        }
        assertNotNull(coupon);
    }

    @Test
    public void testFreeze() {
        Long id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon1");
        ECoupon.freeze(id, null);
        ECoupon eCoupon = ECoupon.findById(id);
        assertEquals(1, eCoupon.isFreeze);
        assertEquals(1, CouponHistory.count());
        List<CouponHistory> historyList = CouponHistory.findAll();
        assertEquals("冻结券号", historyList.get(0).remark);
    }


    @Test
    public void testUnFreeze() {
        Long id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon2");
        ECoupon.unfreeze(id, null);
        ECoupon eCoupon = ECoupon.findById(id);
        assertEquals(0, eCoupon.isFreeze);
        assertEquals(1, CouponHistory.count());
        List<CouponHistory> historyList = CouponHistory.findAll();
        assertEquals("解冻券号", historyList.get(0).remark);
    }

    @Test
    public void testSendMessage() {
        Long id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon2");

        boolean sendFlag = ECoupon.sendMessage(id);
        assertTrue(sendFlag);

        String phone="A3905623568";
        ECoupon.sendUserMessage(id,phone);
        ECoupon eCoupon = ECoupon.findById(id);
        assertEquals(2, eCoupon.downloadTimes.intValue());
        ECoupon.sendUserMessage(id,phone);
        assertEquals(1, eCoupon.downloadTimes.intValue());
        ECoupon.sendUserMessage(id,phone);
        assertEquals(0, eCoupon.downloadTimes.intValue());

    }


}
