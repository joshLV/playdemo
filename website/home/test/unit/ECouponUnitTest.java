package unit;

import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.consumer.User;
import models.order.CouponsCondition;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.order.OrdersCondition;
import models.sales.Goods;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.test.Fixtures;
import play.test.UnitTest;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ECouponUnitTest extends UnitTest {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Before
    public void loadData() {
        Fixtures.delete(User.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(Order.class);
        Fixtures.delete(OrderItems.class);
        Fixtures.delete(ECoupon.class);
        Fixtures.loadModels("fixture/user.yml");
        Fixtures.loadModels("fixture/goods_base.yml");
        Fixtures.loadModels("fixture/goods.yml");
        Fixtures.loadModels("fixture/orders.yml");
        Fixtures.loadModels("fixture/orderItems.yml");

        Long userId = (Long) Fixtures.idCache.get("models.consumer.User-selenium");
        Long orderId = (Long) play.test.Fixtures.idCache.get("models.order.Order-order1");
        Order order = models.order.Order.findById(orderId);
        order.setUser(userId, AccountType.CONSUMER);

        orderId = (Long) play.test.Fixtures.idCache.get("models.order.Order-order2");
        order = models.order.Order.findById(orderId);
        order.setUser(userId, AccountType.CONSUMER);
    }

    /**
     * 测试订单列表
     */
    @Test
    public void testOrder() throws ParseException {
        OrdersCondition condition = new OrdersCondition();
        condition.createdAtBegin = sdf.parse("2012-03-01");
        condition.createdAtEnd = new Date();
        condition.status = OrderStatus.UNPAID;
        condition.goodsName = "哈根";
        int pageNumber = 1;
        int pageSize = 15;
        Long userId = (Long) Fixtures.idCache.get("models.consumer.User-selenium");
        User user = User.findById(userId);
        JPAExtPaginator<Order> list = Order.findUserOrders(user, condition, pageNumber, pageSize);
        assertEquals(1, list.size());

        list = Order.findUserOrders(null, condition, pageNumber, pageSize);
        assertEquals(0, list.size());
    }

    /**
     * 测试券列表
     */
    @Test
    public void testQueryCoupons() throws ParseException {
        CouponsCondition condition = new CouponsCondition();
        condition.createdAtBegin = sdf.parse("2012-03-01");
        condition.createdAtEnd = new Date();

        condition.status = ECouponStatus.UNCONSUMED;
        condition.goodsName = "哈根";
        Long userId = (Long) Fixtures.idCache.get("models.consumer.User-selenium");
        condition.userId = userId;
        condition.accountType = AccountType.CONSUMER;
        int pageNumber = 1;
        int pageSize = 15;
        JPAExtPaginator<ECoupon> list = ECoupon.query(condition, pageNumber, pageSize);
        assertEquals(2, list.size());

    }


    /**
     * 测试退款
     */
    @Test
    public void applyRefund() {
        Long id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon3");
        Long userId = (Long) Fixtures.idCache.get("models.consumer.User-selenium");

        ECoupon eCoupon = ECoupon.findById(id);
        eCoupon.order.userId = userId;
        eCoupon.save();

        String applyNote = "不想要了";
        String ret = ECoupon.applyRefund(null, userId, AccountType.CONSUMER);
        assertEquals("{\"error\":\"no such eCoupon\"}", ret);

        Account account = AccountUtil.getPlatformIncomingAccount();
        account.amount = new BigDecimal("1000000");
        account.save();


        id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon2");
        eCoupon = ECoupon.findById(id);
        ret = ECoupon.applyRefund(eCoupon, userId, AccountType.CONSUMER);
        assertEquals("{\"error\":\"can not apply refund with this goods\"}", ret);
    }

    @Test
    public void getEcouponSn() {
        Long id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon2");
        ECoupon eCoupon = ECoupon.findById(id);
        String sn = eCoupon.getMaskedEcouponSn();
        assertEquals("******7002", sn);
    }

}
