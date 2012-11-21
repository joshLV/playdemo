package unit;

import factory.FactoryBoy;
import models.order.*;
import models.resale.Resaler;
import models.sales.*;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.test.UnitTest;
import util.DateHelper;

import java.util.Date;

import factory.callback.BuildCallback;

public class ResaleHomeOrderUnitTest extends UnitTest {
    Goods goods;
    Resaler resaler;
    Order order;
    OrderItems orderItems;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        FactoryBoy.deleteAll();
        goods = FactoryBoy.create(Goods.class);
        resaler = FactoryBoy.create(Resaler.class);
        order = FactoryBoy.create(Order.class, "paid");
        order.setUser(resaler.getId(), models.accounts.AccountType.RESALER);
        orderItems = FactoryBoy.create(OrderItems.class, new BuildCallback<OrderItems>() {
            @Override
            public void build(OrderItems orderItem) {
                orderItem.createdAt = DateHelper.t("2012-05-01");
            }
        });

    }

    /**
     * 测试订单列表
     */
    @Test
    public void testOrder() {
        OrdersCondition condition = new OrdersCondition();
        condition.createdAtBegin = new Date();
        condition.createdAtEnd = new Date();
        int pageNumber = 1;
        int pageSize = 15;
        JPAExtPaginator<Order> list = Order.findResalerOrders(condition, resaler, pageNumber, pageSize);
        assertEquals(0, list.size());
        condition = new OrdersCondition();
        condition.createdAtBegin = DateHelper.t("2012-03-01");
        condition.createdAtEnd = new Date();
        condition.status = OrderStatus.PAID;
        condition.goodsName = goods.name;
        list = Order.findResalerOrders(condition, resaler, pageNumber, pageSize);
        assertEquals(1, list.size());

    }

}
