package unit;

import models.accounts.AccountType;
import models.consumer.User;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Goods;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import factory.FactoryBoy;
import factory.callback.BuildCallback;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 2/14/12
 * Time: 3:57 PM
 */
public class OrdersTest extends UnitTest {

    private User user;
	private Goods goods;

	@Before
    public void setup() {
    	FactoryBoy.deleteAll();
    	user = FactoryBoy.create(User.class);
		goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
			@Override
			public void build(Goods g) {
				g.limitNumber = 1;
			}
		});		

    }

    @Test
    public void testCheckLimitNumber() {
        // 已经购物
    	FactoryBoy.create(OrderItems.class);
        long boughtNumber = OrderItems.itemsNumber(user, goods.id);
        assertEquals(1l, boughtNumber);
        boolean isBuy = Order.checkLimitNumber(user, goods.id, boughtNumber, 1);
        assertTrue(isBuy);

        Goods goods2 = FactoryBoy.create(Goods.class);
        boughtNumber = OrderItems.itemsNumber(user, goods2.id);
        assertEquals(0l, boughtNumber);
        isBuy = Order.checkLimitNumber(user, goods2.id, boughtNumber, 1);
        assertFalse(isBuy);
    }


}
