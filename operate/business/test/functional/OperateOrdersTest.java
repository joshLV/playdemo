package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.operator.OperateUser;
import models.consumer.User;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Goods;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Test;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.Date;
import java.util.List;

public class OperateOrdersTest extends FunctionalTest {

    Order order;

    @org.junit.Before
    public void setup() {
        FactoryBoy.deleteAll();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        FactoryBoy.create(Goods.class);
        final User user = FactoryBoy.create(User.class);
        order = FactoryBoy.create(Order.class, new BuildCallback<Order>() {
            @Override
            public void build(Order o) {
                o.userId = user.id;
                o.description = "testorder";
                o.paidAt = new Date();
            }
        });
        FactoryBoy.create(OrderItems.class, new BuildCallback<OrderItems>() {
            @Override
            public void build(OrderItems oi) {
                oi.phone = user.mobile;
            }
        });

        OperateUser operateUser = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(operateUser.loginName);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testIndex() {
        Response response = GET("/orders");
        assertStatus(200, response);
        List<Order> orders = (List<Order>) renderArgs("orderList");
        assertEquals(1, orders.size());
    }

    @Test
    public void testDetails() {
        Response response = GET("/orders/" + order.id);
        assertStatus(200, response);
        Order o = (Order) renderArgs("orders");
        List<OrderItems> orderItems = (List<OrderItems>) renderArgs("orderItems");
        assertNotNull(o);
        assertEquals(order.id, o.id);
        assertEquals(1, orderItems.size());
    }

    @Test
    public void testIndexWithCondition() {
        Response response = GET("/orders?condition.createdAtBegin=&condition.createdAtEnd=&condition.paidAtBegin=&condition.paidAtEnd=&condition.brandId=0&condition.refundAtBegin=&condition.refundAtEnd=&condition.payMethod=&condition.userType=&condition.searchKey=&condition.searchItems=&condition.status=&condition.deliveryType=&desc=testorder");
        assertIsOk(response);
        List<Order> orders = (List<Order>) renderArgs("orderList");
        assertEquals(1, orders.size());
        assertNotNull(renderArgs("desc"));
    }
}
