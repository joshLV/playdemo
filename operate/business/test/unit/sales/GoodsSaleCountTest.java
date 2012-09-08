package unit.sales;

import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.sales.Goods;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import factory.FactoryBoy;

public class GoodsSaleCountTest extends UnitTest {

	@Before
	public void setUp() {
		FactoryBoy.lazyDelete();
	}
	
	@Test
	public void testGetCurrentSaleCount1() {
		OrderItems orderItems = FactoryBoy.create(OrderItems.class);
		Goods goods = orderItems.goods;
		goods.baseSale = 20l;
		goods.save();
		Order order = orderItems.order;
		order.status = OrderStatus.PAID;
		order.save();
		
		assertEquals(new Long(1), goods.getCurrentSaleCount());
		assertEquals(new Long(19), goods.getCurrentBaseSale());
		
		// 取消订单后库存释放
		assertEquals(1, order.orderItems.size());
		order.status = OrderStatus.CANCELED;
		order.save();

		assertEquals(new Long(0), goods.getCurrentSaleCount());
		assertEquals(new Long(20), goods.getCurrentBaseSale());
	}
}
