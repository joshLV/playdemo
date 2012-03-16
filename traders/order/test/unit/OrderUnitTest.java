package unit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.consumer.Address;
import models.consumer.User;
import models.order.Cart;
import models.order.ECoupon;
import models.order.NotEnoughInventoryException;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.order.Orders;
import models.sales.Goods;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;


public class OrderUnitTest extends UnitTest {
	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		Fixtures.delete(models.order.Orders.class);
		Fixtures.delete(models.order.OrderItems.class);
		Fixtures.delete(models.sales.Goods.class);
		Fixtures.delete(models.consumer.User.class);
		Fixtures.loadModels("fixture/goods_base.yml","fixture/user.yml","fixture/goods.yml",
				"fixture/orders.yml");
	}

	/**
	 * 测试订单列表
	 */
	@Test
	public void testOrder() {
		Orders orders = new Orders();
		orders.createdAtBegin = new Date();
		orders.createdAtEnd = new Date();
		orders.status = OrderStatus.UNPAID;
		orders.deliveryType = 1;
		orders.payMethod = "1";
		Long compnayId = 1l;
		orders.searchKey = "2";
		orders.searchItems = "2012";
		int pageNumber = 1;
		int pageSize = 15;
		List<Orders> list = Orders.query(orders, compnayId, pageNumber, pageSize);
		assertEquals(0, list.size());

		orders = new Orders();
		SimpleDateFormat sdf  =   new  SimpleDateFormat( "yyyy-MM-dd" );  
		try {
			orders.createdAtBegin = sdf.parse("2012-03-01");
			orders.createdAtEnd = new Date();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		orders.status = OrderStatus.PAID;
		orders.deliveryType = 1;
		orders.payMethod = "alipay";
		orders.searchKey = "2";
		orders.searchItems = "2012";
		list = Orders.query(orders, compnayId, pageNumber, pageSize);
		assertEquals(1, list.size());

		orders = new Orders();
		orders.searchKey = "1";
		orders.searchItems = "哈根达斯200";
		list = Orders.query(orders, compnayId, pageNumber, pageSize);
		assertEquals(1, list.size());

		orders = new Orders();
		try {
			orders.refundAtBegin = sdf.parse("2012-03-01");
			orders.refundAtEnd = new Date();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		list = Orders.query(orders, compnayId, pageNumber, pageSize);
		assertEquals(0, list.size());
	}


	@Test
	public void testOrders() {
		User user=new User();
		user.loginName="y";
		Address address=new Address();
		address.mobile = "13000000000";
		address.name = " 徐家汇";
		address.postcode = "200120";
		Orders orders = new Orders(user,address);
		assertNotNull(orders);
	}

	@Test
	public void testOrdersNumber() {
		String mobile ="1310000000";
		User user=new User();
		user.loginName="y";
		Address address=new Address();
		address.mobile = "13000000000";
		address.name = " 徐家汇";
		address.postcode = "200120";
		Long goodsId = (Long) Fixtures.idCache.get("models.sales" +
				".Goods-Goods_001");
		boolean isok= false;
		try {
			Orders orders = new Orders(user, goodsId, 2l, address, mobile);
			assertNotNull(orders);
			new Orders(user, goodsId, 200000l, address, mobile);
		} catch (NotEnoughInventoryException e) {
			isok =true;
		}
		assertEquals(true,isok);
	}

	@Test
	public void testOrdersCart() {
		String mobile ="1310000000";
		User user=new User();
		user.loginName="y";
		Address address=new Address();
		address.mobile = "13000000000";
		address.name = " 徐家汇";
		address.postcode = "200120";
		List<Cart> cartList = new ArrayList();
		Long goodsId = (Long) Fixtures.idCache.get("models.sales" +
				".Goods-Goods_001");
		Goods goods = Goods.findById(goodsId);
		Cart cart = new Cart(goods,2l);
		cartList.add(cart);
		try {
			Orders orders = new Orders(user, cartList, address, mobile);
			assertNotNull(orders);
		} catch (NotEnoughInventoryException e) {
			e.printStackTrace();
		}

	}
	@Test
	public void testPaid() {
		Long goodsId = (Long) Fixtures.idCache.get("models.sales" +
				".Goods-Goods_001");
		Goods goods = Goods.findById(goodsId);
		int saleCount= goods.saleCount;
		int baseSale = goods.baseSale.intValue();
		Long orderId = (Long) Fixtures.idCache.get("models.order" +
				".Orders-order1");
		Orders orders = Orders.findById(orderId);
		orders.paid();
		assertEquals(OrderStatus.PAID,orders.status);
		assertEquals(saleCount+1,goods.saleCount);
		assertEquals(baseSale-1,goods.baseSale.intValue());
	}
	@Test
	public void testItemsNumber() {
		Long orderId = (Long) Fixtures.idCache.get("models.order" +
				".Orders-order1");
		Orders orders = Orders.findById(orderId);
		Long itemsNumber = Orders.itemsNumber(orders);
		assertEquals(itemsNumber.intValue(),2);
	}
	

}
