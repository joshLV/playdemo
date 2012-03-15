import java.util.Date;
import java.util.List;

import models.consumer.User;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.OrderStatus;
import models.order.Orders;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import play.modules.paginate.JPAExtPaginator;
import play.test.Fixtures;
import play.test.UnitTest;


public class OrderUnitTest extends UnitTest {

	@Before
	public void loadData() {
		Fixtures.deleteAllModels();
		Fixtures.loadModels("fixture/base_data.yml");
		Fixtures.loadModels("fixture/user.yml");
		Fixtures.loadModels("fixture/goods.yml");
		Fixtures.loadModels("fixture/orders.yml");
	}

	/**
	 * 测试订单列表
	 */
	@Test
	public void testOrder(){
		User user = new User();
		user.id=2l;
		Date createdAtBegin=new Date();
		Date createdAtEnd=new Date();
		OrderStatus status= null;
		String goodsName="";
		int pageNumber =1;
		int pageSize =15;
		JPAExtPaginator<Orders> list = Orders.findMyOrders(user, createdAtBegin, createdAtEnd, status, goodsName,pageNumber, pageSize);
		Assert.assertEquals(0,list.size());

	}

	/**
	 * 测试券列表
	 */
	@Test
	public void testQueryCoupons(){
		User user = new User();
		user.id=2l;
		Date createdAtBegin=new Date();
		Date createdAtEnd=new Date();
		String goodsName="";
		int pageNumber =1;
		int pageSize =15;
		ECouponStatus status =null;
		JPAExtPaginator<ECoupon> list = ECoupon.userCouponsQuery(user,createdAtBegin,createdAtEnd, status, goodsName,pageNumber, pageSize);
		Assert.assertEquals(0,list.size());

	}

}
