package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.operator.OperateUserFactory;
import factory.callback.BuildCallback;
import models.operator.OperateRole;
import models.operator.OperateUser;
import models.consumer.User;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Goods;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OperateOrdersForSalesManagerTest extends FunctionalTest {

    Order order;
    OperateUser operateUser;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        // only sales role.
        operateUser = FactoryBoy.create(OperateUser.class, new BuildCallback<OperateUser>() {
            @Override
            public void build(OperateUser user) {
                // 定义角色
                user.roles = new ArrayList<OperateRole>();
                user.roles.add(OperateUserFactory.role("sales"));
                user.roles.add(OperateUserFactory.role("manager"));
            }
        });
        // 设置测试登录的用户名
        Security.setLoginUserForTest(operateUser.loginName);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void 可以查看其它人的订单() {
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

        Response response = GET("/orders");
        assertStatus(200, response);
        List<Order> orders = (List<Order>) renderArgs("orderList");
        assertEquals(1, orders.size());
    }

    @Test
    public void 可以查看自己的订单() {
        FactoryBoy.create(Supplier.class, new BuildCallback<Supplier>() {
            @Override
            public void build(Supplier s) {
                s.salesId = operateUser.id;
            }
        });
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

        Response response = GET("/orders");
        assertStatus(200, response);
        List<Order> orders = (List<Order>) renderArgs("orderList");
        assertEquals(1, orders.size());
    }

}
