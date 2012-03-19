import models.consumer.User;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.Date;


public class OrderUnitTest extends UnitTest {

	@Before
	public void loadData() {
		Fixtures.deleteAllModels();
		Fixtures.loadModels("fixture/user.yml");
		Fixtures.loadModels("fixture/goods_base.yml");
		Fixtures.loadModels("fixture/goods.yml");
		Fixtures.loadModels("fixture/orders.yml");
		Fixtures.loadModels("fixture/orderItems.yml");
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
		JPAExtPaginator<Order> list = Order.findMyOrders(user, createdAtBegin, createdAtEnd, status, goodsName,pageNumber, pageSize);
		assertEquals(0,list.size());

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
		assertEquals(0,list.size());

	}


	/**
	 * 测试退款
	 */
	@Test
	public void applyRefund(){
		Long id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon2");
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-selenium");
		ECoupon eCoupon=ECoupon.findById(id);
		String applyNote="不想要了";
		String ret = ECoupon.applyRefund(eCoupon,userId,applyNote);
		assertEquals("{\"error\":\"can not get the trade bill\"}",ret);
		
		ret = ECoupon.applyRefund(null,userId,applyNote);
		assertEquals("{\"error\":\"no such eCoupon\"}",ret);
		
		id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon1");
		eCoupon=ECoupon.findById(id);
		ret = ECoupon.applyRefund(eCoupon,userId,applyNote);
		assertEquals("{\"error\":\"can not apply refund with this goods\"}",ret);
	}

}
