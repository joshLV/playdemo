package unit;

import models.consumer.User;
import models.job.CancelUnPaidOrderJob;
import models.order.CancelUnpaidOrders;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Goods;
import org.junit.Before;
import org.junit.Test;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import play.test.Fixtures;
import play.test.UnitTest;
import util.DateHelper;

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
        FactoryBoy.deleteAll();
    }

    @Test
    public void testJob() throws ParseException {
        final User user = FactoryBoy.create(User.class);
        Goods goods = FactoryBoy.create(Goods.class);
        Order order = FactoryBoy.create(Order.class, new BuildCallback<Order>() {
			@Override
			public void build(Order o) {
				o.createdAt = DateHelper.beforeDays(12); //12天以前的订单
				o.userId = user.id;
			}
		});
        OrderItems orderItems = FactoryBoy.create(OrderItems.class);
        orderItems.phone = user.mobile;
        orderItems.save();
        
        order.refresh();
     
        long realStocks = goods.getRealStocks();
        long realSaleCount = goods.getRealSaleCount();
        
        assertEquals(0, CancelUnpaidOrders.count());

        CancelUnPaidOrderJob job = new CancelUnPaidOrderJob();
        job.doJob();

        assertEquals(1, CancelUnpaidOrders.count());
        assertEquals(new Long(realStocks + 1), goods.getRealStocks());
        assertEquals(new Long(realSaleCount - 1), goods.getRealSaleCount());

        // 再次执行job，应该没有变化 
        job.doJob();

        assertEquals(1, CancelUnpaidOrders.count());
        assertEquals(new Long(realStocks + 1), goods.getRealStocks());
        assertEquals(new Long(realSaleCount - 1), goods.getRealSaleCount());

    }
}


