import java.util.List;

import models.order.Orders;
import models.sales.Goods;

import org.junit.Assert;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.UnitTest;


public class OrderUnitTest extends UnitTest {
	@Test
	public void testOrder(){
		Orders orders=new Orders();
		orders.createdAtBegin="2012-02-20 00:00:00";
		orders.createdAtEnd="2012-02-20 23:59:59";
		orders.status="UPPAID";
		orders.deliveryType=1;
		orders.payMethod="1";
		List<Orders> list = 	Orders.query(orders);  
		Assert.assertEquals(0,list.size());  

	}
}
