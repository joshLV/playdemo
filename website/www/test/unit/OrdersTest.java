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

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 2/14/12
 * Time: 3:57 PM
 */
public class OrdersTest extends UnitTest {

    @Before
    public void setup() {
        Fixtures.delete(OrderItems.class);
        Fixtures.delete(Order.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(User.class);
        Fixtures.loadModels("fixture/user.yml");
        Fixtures.loadModels("fixture/goods.yml");
    }

    @Test
    public void testCheckLimitNumber() {
        Long id = (Long) Fixtures.idCache.get("models.consumer.User-user");
        User user = User.findById(id);

        Long orderId = (Long) Fixtures.idCache.get("models.order.Order-order10");
        Order order = Order.findById(orderId);
        order.userType = AccountType.CONSUMER;
        order.userId = id;
        order.save();
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-goods5");
        Long boughtNumber = OrderItems.itemsNumber(user, goodsId);
        boolean isBuy = Order.checkLimitNumber(user, goodsId, boughtNumber, 1);
        assertTrue(isBuy);

        goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-goods4");
        boughtNumber = OrderItems.itemsNumber(user, goodsId);
        isBuy = Order.checkLimitNumber(user, goodsId, boughtNumber, 1);
        assertFalse(isBuy);


    }


}
