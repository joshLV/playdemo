package unit;

import models.consumer.User;
import models.job.CancleUnPaidOrderJob;
import models.order.CancelUnpaidOrders;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Goods;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-6-20
 * Time: 下午2:14
 */
public class CancelOrdersUnitTest extends UnitTest {
    @Before
    public void setup() {
        Fixtures.delete(OrderItems.class);
        Fixtures.delete(Order.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(User.class);
        Fixtures.delete(CancelUnpaidOrders.class);
        Fixtures.loadModels("fixture/user.yml");
        Fixtures.loadModels("fixture/goods_cancel_order.yml");
        Fixtures.loadModels("fixture/test_cancel_orders.yml");
    }

    @Test
    public void testJob() throws ParseException {
        List<Order> orderList = Order.findAll();
        for (Order order : orderList) {
            if ("2012034997599".equals(order.orderNumber)) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 11);
                order.createdAt = calendar.getTime();
                order.save();
            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 10);
                order.createdAt = calendar.getTime();
                order.save();
            }
        }

        Long id = (Long) Fixtures.idCache.get("models.sales.Goods-goods1");
        Goods goods = Goods.findById(id);
        assertEquals(1000, goods.baseSale.intValue());
        assertEquals(100, goods.saleCount);
        int count = CancelUnpaidOrders.findAll().size();
        assertEquals(0, count);

        CancleUnPaidOrderJob job = new CancleUnPaidOrderJob();
        job.doJob();

        count = CancelUnpaidOrders.findAll().size();
        assertEquals(2, count);
        goods = Goods.findById(id);
        assertEquals(1003, goods.baseSale.intValue());
        assertEquals(97, goods.saleCount);


        job.doJob();
        count = CancelUnpaidOrders.findAll().size();
        assertEquals(2, count);
        assertEquals(1003, goods.baseSale.intValue());
        assertEquals(97, goods.saleCount);
    }
}


