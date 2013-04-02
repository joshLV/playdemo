package functional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.accounts.Account;
import models.accounts.util.AccountUtil;
import models.consumer.User;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.order.OrdersCondition;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.modules.breadcrumbs.BreadcrumbList;
import play.mvc.Http;
import play.test.FunctionalTest;
import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.callback.SequenceCallback;

/**
 * 用户订单功能测试.
 * <p/>
 * User: Juno
 * Date: 12-7-30
 * Time: 下午4:37
 */
public class UserOrdersFuncTest extends FunctionalTest {

    User user;
    Goods goods;
    Order order;
    OrderItems orderItems;
    ECoupon ecoupon;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();

        //设置虚拟登陆
        // 设置测试登录的用户名
        user = FactoryBoy.create(User.class);
        Security.setLoginUserForTest(user.loginName);

        FactoryBoy.create(Supplier.class);
        FactoryBoy.create(Shop.class);

        goods = FactoryBoy.create(Goods.class);
        order = FactoryBoy.create(Order.class, new BuildCallback<Order>() {
            @Override
            public void build(Order o) {
                o.userId = user.id;
                o.accountPay = goods.salePrice;
            }
        });
        orderItems = FactoryBoy.create(OrderItems.class, new BuildCallback<OrderItems>() {
            @Override
            public void build(OrderItems oi) {
                oi.phone = user.mobile;
                oi.buyNumber = 3l;
            }
        });
        List<ECoupon> ecoupons = FactoryBoy.batchCreate(3, ECoupon.class, new SequenceCallback<ECoupon>() {
            @Override
            public void sequence(ECoupon ecoupon, int seq) {
                // do nothing.
            }
        });
        ecoupon = ecoupons.get(0);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/orders");
        assertStatus(200, response);

        List<Order> orderList = (List<Order>) renderArgs("orderList");
        final BreadcrumbList breadcrumbs = (BreadcrumbList) renderArgs("breadcrumbs");
        final OrdersCondition condition = (OrdersCondition) renderArgs("condition");
        assertEquals(1, orderList.size());
        assertEquals(user.id, ((User) renderArgs("user")).id);
        assertEquals(1, breadcrumbs.size());
        assertEquals("我的订单", breadcrumbs.get(0).desc);
        assertEquals("/orders", breadcrumbs.get(0).url);
        assertNotNull(condition);
    }


    @Test
    public void testPay() {
        Http.Response response = GET("/payment/" + order.orderNumber);
        assertStatus(302, response);
    }

    /**
     * 测试显示退款信息页面.
     */
    @Test
    public void testRefund() {
        Account account = AccountUtil.getPlatformIncomingAccount();
        account.amount = new BigDecimal("10000");
        account.save();

        assertEquals(OrderStatus.UNPAID, order.status);

        Http.Response response = GET("/orders/refund/" + order.orderNumber);
        assertStatus(200, response);
        assertEquals(order, (Order) renderArgs("order"));

        Order resultOrder = (Order) renderArgs("order");
        assertEquals(order.orderNumber, resultOrder.orderNumber);
        List<ECoupon> eCoupons = (List<ECoupon>) renderArgs("eCoupons");
        assertEquals(3, eCoupons.size());
        BreadcrumbList breadcrumbs = (BreadcrumbList) renderArgs("breadcrumbs");
        assertNotNull(breadcrumbs);
    }

    /**
     * 测试退款功能.
     */
    @Test
    public void testBatchRefund() {
        assertEquals(OrderStatus.UNPAID, order.status);

        Map<String, String> args = new HashMap<>();
        args.put("orderNumber", order.orderNumber);
        args.put("couponIds", ecoupon.id.toString());

        Account account = AccountUtil.getPlatformIncomingAccount();
        BigDecimal originAmount = new BigDecimal("10000");
        account.amount = originAmount;
        account.save();

        Http.Response response = POST("/orders/batch-refund", args);
        assertStatus(302, response);

        ECoupon resultECoupon = ECoupon.findById(ecoupon.id);
        resultECoupon.refresh();
        assertEquals(ECouponStatus.REFUND, resultECoupon.status);
        assertNotNull(resultECoupon.refundAt);
        assertEquals(0, resultECoupon.refundPrice.compareTo(resultECoupon.salePrice));
        account.refresh();
        assertEquals(originAmount.subtract(goods.salePrice).setScale(2), account.amount.setScale(2));
    }

    @Test
    public void testCancelOrder() {
        assertEquals(OrderStatus.UNPAID, order.status);

        Http.Response response = PUT("/orders/" + order.orderNumber + "/cancel", "text/html", "");
        assertStatus(200, response);
        Order updatedOrder = Order.findById(order.id);
        updatedOrder.refresh();
        assertEquals(OrderStatus.CANCELED, updatedOrder.status);
    }

    @Test
    public void testDetails() {
        Http.Response response = GET("/orders/" + order.orderNumber);
        assertStatus(200, response);
        assertEquals(order, (Order) renderArgs("order"));
    }
}
