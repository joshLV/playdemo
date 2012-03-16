package unit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import models.order.ECoupon;
import models.order.OrderStatus;
import models.order.Orders;

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
		Fixtures.loadModels("fixture/goods_base.yml","fixture/goods.yml", "fixture/orders.yml", "fixture/user.yml");
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
		SimpleDateFormat sdf  =   new  SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );  
		try {
			orders.createdAtBegin = sdf.parse("2012-03-01");
			orders.createdAtEnd = new Date();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		orders.status = OrderStatus.PAID;
		orders.deliveryType = 1;
		orders.paymentSourceCode = "alipay";
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
		orders.refundAtEnd = new Date();
		list = Orders.query(orders, compnayId, pageNumber, pageSize);
		assertEquals(0, list.size());
	}
}
