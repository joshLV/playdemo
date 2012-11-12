package unit;

import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.consumer.User;
import models.order.CouponsCondition;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderStatus;
import models.order.OrdersCondition;
import models.sales.Goods;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.test.UnitTest;
import util.DateHelper;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;


public class ECouponUnitTest extends UnitTest {
    User user;
    Goods goods;
    Order order;
    ECoupon eCoupon;

    @Before
    public void loadData() {
        FactoryBoy.deleteAll();
        user = FactoryBoy.create(User.class);
        order = FactoryBoy.create(Order.class);
        order.setUser(user.id, AccountType.CONSUMER);
        eCoupon = FactoryBoy.create(ECoupon.class);
    }

    /**
     * 测试订单列表
     */
    @Test
    public void testOrder() throws ParseException {
        OrdersCondition condition = new OrdersCondition();
        condition.createdAtBegin = DateHelper.beforeDays(new Date(), 1);
        condition.createdAtEnd = new Date();
        condition.status = OrderStatus.UNPAID;
        condition.goodsName = "哈根";
        int pageNumber = 1;
        int pageSize = 15;
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
        condition.createdAtBegin = DateHelper.beforeDays(new Date(), 1);
        condition.createdAtEnd = new Date();
        condition.status = ECouponStatus.UNCONSUMED;
        condition.goodsName = "哈根";
        condition.userId = user.id;
        condition.accountType = AccountType.CONSUMER;
        int pageNumber = 1;
        int pageSize = 15;
        JPAExtPaginator<ECoupon> list = ECoupon.query(condition, pageNumber, pageSize);
        assertEquals(2, list.size());

        list = ECoupon.getUserCoupons(condition, pageNumber, pageSize);
        assertEquals(2, list.size());

    }


    /**
     * 测试退款
     */
    @Test
    @Ignore
    public void applyRefund() {

        eCoupon.order.userId = user.id;
        eCoupon.save();

        String applyNote = "不想要了";
        String ret = ECoupon.applyRefund(null, user.id, AccountType.CONSUMER);
        assertEquals("{\"error\":\"no such eCoupon\"}", ret);

        Account account = AccountUtil.getPlatformIncomingAccount();
        account.amount = new BigDecimal("1000000");
        account.save();


        ret = ECoupon.applyRefund(eCoupon, user.id, AccountType.CONSUMER);
        assertEquals("{\"error\":\"can not apply refund with this goods\"}", ret);
    }

    @Test
    public void getEcouponSn() {
        String sn = eCoupon.getMaskedEcouponSn();
        System.out.println(sn
        );
        assertEquals("******7002", sn);
    }

}
