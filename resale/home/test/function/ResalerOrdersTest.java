package function;

import controllers.modules.resale.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.callback.SequenceCallback;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.resale.Resaler;
import models.sales.Goods;
import org.junit.Before;
import org.junit.Test;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.test.FunctionalTest;
import util.DateHelper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 13-1-6
 * Time: 下午5:33
 */
public class ResalerOrdersTest extends FunctionalTest {
    Goods goods;
    Resaler resaler;
    Order order;
    OrderItems orderItems;
    ECoupon ecoupon;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();

        resaler = FactoryBoy.create(Resaler.class);


        FactoryBoy.batchCreate(5, Order.class, new BuildCallback<Order>() {
            @Override
            public void build(Order o) {
                o.userType = AccountType.RESALER;
                o.userId = resaler.id;
                o.createdAt = DateHelper.beforeDays(1);
            }
        });

        order = FactoryBoy.create(Order.class);
        order.orderNumber = "123456";
        order.userId = resaler.id;
        order.userType = AccountType.RESALER;
        order.save();

        orderItems = FactoryBoy.create(OrderItems.class, new BuildCallback<OrderItems>() {
            @Override
            public void build(OrderItems oi) {
                oi.buyNumber = 3l;
                oi.order = order;
            }
        });
        List<ECoupon> ecoupons = FactoryBoy.batchCreate(3, ECoupon.class, new SequenceCallback<ECoupon>() {
            @Override
            public void sequence(ECoupon ecoupon, int seq) {
                ecoupon.order = order;
                ecoupon.orderItems = orderItems;
            }
        });

        ecoupon = ecoupons.get(0);
        Security.setLoginUserForTest(resaler.loginName);
    }

    @Test
    public void testDetails() {
        Http.Response response = GET("/orders/" + order.orderNumber);
        assertStatus(200, response);
        Order o = (Order) renderArgs("order");
        assertNotNull(o);
        assertEquals(order.id, o.id);
        final BreadcrumbList breadcrumbs = (BreadcrumbList) renderArgs("breadcrumbs");
        assertEquals("我的订单", breadcrumbs.get(0).desc);
        assertEquals("/orders", breadcrumbs.get(0).url);
    }

    @Test
    public void testOrders_noCondition() {
        Http.Response response = GET("/orders");
        assertIsOk(response);
        JPAExtPaginator<Order> orderList = (JPAExtPaginator<Order>) renderArgs("orderList");
        assertEquals(6, orderList.getRowCount());
        final BreadcrumbList breadcrumbs = (BreadcrumbList) renderArgs("breadcrumbs");
        assertEquals(resaler, renderArgs("resaler"));
        assertEquals(1, breadcrumbs.size());
        assertEquals("我的订单", breadcrumbs.get(0).desc);
        assertEquals("/orders", breadcrumbs.get(0).url);
    }

    @Test
    public void testOrders_haveCondition() {
        Http.Response response = GET("/orders?condition.createdAtBegin=" + new Date());
        assertIsOk(response);
        JPAExtPaginator<Order> orderList = (JPAExtPaginator<Order>) renderArgs("orderList");
        assertNotNull(orderList);
        assertEquals(6, orderList.getRowCount());
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
        Map<String, String> params = new HashMap<>();
        params.put("couponIds[]", ecoupon.id.toString());
        params.put("orderNumber", order.orderNumber);
        Http.Response response = POST("/orders/batch-refund", params);
        assertStatus(302, response);
        ecoupon.refresh();
        assertEquals(ecoupon.status, ECouponStatus.REFUND);
    }
}
