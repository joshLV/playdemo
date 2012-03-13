package unit;

import java.util.Date;
import java.util.List;

import models.order.ECoupon;
import models.order.OrderStatus;
import models.order.Orders;

import org.junit.Assert;
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
		Fixtures.loadModels("fixture/goods_base.yml","fixture/orders.yml","fixture/user.yml");
	}

	/**
	 * 测试订单列表
	 */
	@Test
	public void testOrder(){
		Orders orders=new Orders();
		orders.createdAtBegin=new Date();
		orders.createdAtEnd=new Date();
		orders.status=OrderStatus.UNPAID;
		orders.deliveryType=1;
		orders.payMethod="1";
		Long compnayId =1l;
		orders.searchKey="2";
		orders.searchItems="2012";
		int pageNumber =1;
		int pageSize =15;
		List<Orders> list = Orders.query(orders,compnayId,pageNumber,pageSize);
		Assert.assertEquals(0,list.size());  

	}

	/**
	 * 测试券列表
	 */
	@Test
	public void testQueryCoupons(){
		Long compnayId =1l;
		int pageNumber =1;
		int pageSize =15;
		List<ECoupon> list = ECoupon.queryCoupons(compnayId,pageNumber,pageSize);
		Assert.assertEquals(0,list.size());

	}
}
