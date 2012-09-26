package functional;

import controllers.modules.website.cas.Security;
import models.accounts.Account;
import models.accounts.util.AccountUtil;
import models.consumer.User;
import models.consumer.UserInfo;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.modules.breadcrumbs.BreadcrumbList;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户订单功能测试.
 * <p/>
 * User: Juno
 * Date: 12-7-30
 * Time: 下午4:37
 */
public class UserOrdersFuncTest extends FunctionalTest {

    @Before
    public void setup() {
        Fixtures.delete(User.class);
        Fixtures.delete(UserInfo.class);
        Fixtures.delete(Order.class);
        Fixtures.delete(OrderItems.class);
        Fixtures.delete(ECoupon.class);
        Fixtures.loadModels("fixture/user.yml");
        Fixtures.loadModels("fixture/userInfo.yml");
        Fixtures.loadModels("fixture/supplier_unit.yml");
        Fixtures.loadModels("fixture/shops.yml");
        Fixtures.loadModels("fixture/brands.yml");
        Fixtures.loadModels("fixture/categories_unit.yml");
        Fixtures.loadModels("fixture/goods.yml");
        Fixtures.loadModels("fixture/orders.yml");
        Fixtures.loadModels("fixture/orderItems.yml");

        Long userId = (Long) Fixtures.idCache.get("models.consumer.User-selenium");
        User user = User.findById(userId);

        //设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testPay() {
        long id = (Long) Fixtures.idCache.get("models.order.Order-order1");
        Order order = Order.findById(id);
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

        long id = (Long) Fixtures.idCache.get("models.order.Order-order1");
        Order order = Order.findById(id);
        assertEquals(OrderStatus.UNPAID, order.status);
        order.userId = (Long) Fixtures.idCache.get("models.consumer.User-selenium");
        order.save();
        Http.Response response = GET("/orders/refund/" + order.orderNumber);
        assertStatus(200, response);

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
        long id = (Long) Fixtures.idCache.get("models.order.Order-order1");
        Order order = Order.findById(id);
        assertEquals(OrderStatus.UNPAID, order.status);
        order.userId = (Long) Fixtures.idCache.get("models.consumer.User-selenium");
        order.save();

        long couponId = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon1");
        String couponIds = String.valueOf(couponId);
        Map<String, String> args = new HashMap<>();
        args.put("orderNumber", order.orderNumber);
        args.put("couponIds", couponIds);

        Account account = AccountUtil.getPlatformIncomingAccount();
        account.amount = new BigDecimal("10000");
        account.save();
        Http.Response response = POST("/orders/batch-refund", args);
        assertStatus(302, response);

        ECoupon eCoupon = ECoupon.findById(couponId);
        assertEquals(ECouponStatus.REFUND, eCoupon.status);
        assertNotNull(eCoupon.refundAt);
        assertEquals(0, eCoupon.refundPrice.compareTo(eCoupon.salePrice));
    }

    @Test
    public void testCancelOrder() {

        long id = (Long) Fixtures.idCache.get("models.order.Order-order1");
        Order order = Order.findById(id);
        assertEquals(OrderStatus.UNPAID, order.status);
        order.userId = (Long) Fixtures.idCache.get("models.consumer.User-selenium");
        order.save();

        Http.Response response = PUT("/orders/" + order.orderNumber + "/cancel", "text/html", "");
        assertStatus(200, response);
        Order updatedOrder = Order.findById(id);
        updatedOrder.refresh();
        assertEquals(OrderStatus.CANCELED, updatedOrder.status);
    }
}
