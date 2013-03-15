package unit;

import models.accounts.AccountType;
import models.consumer.User;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Goods;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import factory.FactoryBoy;
import factory.callback.BuildCallback;

/**
 * OrderItems单元测试.
 * <p/>
 * User: sujie
 * Date: 2/14/12
 * Time: 3:57 PM
 */
public class OrderItemsUnitTest extends UnitTest {

    private User user;
    private Goods goods;
    private OrderItems items;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        user = FactoryBoy.create(User.class);
        goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.limitNumber = 1L;
            }
        });
        items = FactoryBoy.create(OrderItems.class);
    }

    @Test
    public void testCheckLimitNumber() {
        // 已经购物
        long boughtNumber = OrderItems.itemsNumber(user, goods.id);
        assertEquals(1l, boughtNumber);
        boolean isBuy = Order.checkLimitNumber(goods.id, boughtNumber, 1);
        assertTrue(isBuy);

        Goods goods2 = FactoryBoy.create(Goods.class);
        boughtNumber = OrderItems.itemsNumber(user, goods2.id);
        assertEquals(0l, boughtNumber);
        isBuy = Order.checkLimitNumber(goods2.id, boughtNumber, 1);
        assertFalse(isBuy);
    }

    @Test
    public void testGetUnpaidOrderCount() {
        Order order = FactoryBoy.create(Order.class);
        OrderItems orderItems = FactoryBoy.create(OrderItems.class);
        orderItems.order = order;
        orderItems.save();

        long count = OrderItems.getUnpaidOrderCount(user.id, AccountType.CONSUMER);
        assertEquals(2, count);
    }

}
