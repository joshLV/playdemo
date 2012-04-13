package unit;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import models.accounts.AccountType;
import models.consumer.Address;
import models.consumer.User;
import models.order.NotEnoughInventoryException;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.order.OrdersCondition;
import models.sales.Goods;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;


public class OrderUnitTest extends UnitTest {
	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		Fixtures.delete(models.order.OrderItems.class);
		Fixtures.delete(models.order.Order.class);
		Fixtures.delete(models.sales.Goods.class);
		Fixtures.delete(User.class);
		Fixtures.loadModels("fixture/goods_base.yml", 
				"fixture/user.yml",
				"fixture/goods.yml",
				"fixture/orders.yml", "fixture/orderItems.yml");
	}

	/**
	 * 测试订单列表
	 */
	@Test
	public void testOrder() {
		OrdersCondition order = new OrdersCondition();
		order.createdAtBegin = new Date();
		order.createdAtEnd = new Date();
		order.status = OrderStatus.UNPAID;
		order.deliveryType = 1;
		order.payMethod = "1";
		Long supplierId = 1l;
		order.searchKey = "2";
		order.searchItems = "2012";
		int pageNumber = 1;
		int pageSize = 15;
		List<Order> list = Order.query(order, supplierId, pageNumber, pageSize);
		assertEquals(0, list.size());

		order = new OrdersCondition();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			order.createdAtBegin = sdf.parse("2012-03-01");
			order.createdAtEnd = new Date();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		order.status = OrderStatus.PAID;
		order.deliveryType = 1;
		order.payMethod = "alipay";
		order.searchKey = "2";
		order.searchItems = "2012";
		list = Order.query(order, supplierId, pageNumber, pageSize);
		assertEquals(1, list.size());

		order = new OrdersCondition();
		order.searchKey = "1";
		order.searchItems = "哈根达斯200";
		list = Order.query(order, supplierId, pageNumber, pageSize);
		assertEquals(1, list.size());

		order = new OrdersCondition();
		try {
			order.refundAtBegin = sdf.parse("2012-03-01");
			order.refundAtEnd = new Date();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		list = Order.query(order, supplierId, pageNumber, pageSize);
		assertEquals(0, list.size());
	}

	@Test
	public void testOrdersNumber() {
		String mobile = "1310000000";
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
		Address address = new Address();
		address.mobile = "13000000000";
		address.name = " 徐家汇";
		address.postcode = "200120";
		Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
		BigDecimal resalePrice = new BigDecimal("10.2");
		boolean isOk = false;
		try {
			Goods oldGoods = Goods.findById(goodsId);
			int baseSale = oldGoods.baseSale.intValue();
			int saleCount =oldGoods.saleCount;
			Order order = new Order(userId, AccountType.CONSUMER);
			order.addOrderItem((Goods)Goods.findById(goodsId), 20L, mobile, oldGoods.salePrice, resalePrice);
			order.createAndUpdateInventory();
			
			Goods goods = Goods.findById(goodsId);
			assertEquals(baseSale-20, goods.baseSale.intValue());
			assertEquals(saleCount+20, goods.saleCount);
			
			//异常情况
			order.addOrderItem((Goods)Goods.findById(goodsId), 200000L, mobile, goods.salePrice,resalePrice);

		} catch (NotEnoughInventoryException e) {
			isOk = true;
		}
		assertEquals(true, isOk);
	}


	@Test
	public void testPaid() {
		Long orderId = (Long) Fixtures.idCache.get("models.order.Order-order1");
		Order orders = Order.findById(orderId);
		orders.paid();
		assertEquals(OrderStatus.PAID, orders.status);
	}

	@Test
	public void testItemsNumber() {
		Long orderId = (Long) Fixtures.idCache.get("models.order.Order-order1");
		Order orders = Order.findById(orderId);
		long itemsNumber = OrderItems.itemsNumber(orders);
		assertEquals(2L, itemsNumber);
	}


}
