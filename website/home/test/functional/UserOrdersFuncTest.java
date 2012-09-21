package functional;

import controllers.modules.website.cas.Security;
import models.consumer.User;
import models.consumer.UserInfo;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;

/**
 * Created with IntelliJ IDEA.
 * User: Juno
 * Date: 12-7-30
 * Time: 下午4:37
 * To change this template use File | Settings | File Templates.
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

    //@Test TODO 功能不可用
    public void testBatchRefund() {

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
