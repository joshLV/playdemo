package unit;

import models.accounts.Account;
import models.consumer.User;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Area;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class VerificationUnitTest extends UnitTest {
	@Before
	public void setup() {
		Fixtures.delete(Category.class);
		Fixtures.delete(Brand.class);
		Fixtures.delete(Area.class);
		Fixtures.delete(Order.class);
		Fixtures.delete(OrderItems.class);
		Fixtures.delete(Goods.class);
		Fixtures.delete(User.class);
		Fixtures.delete(ECoupon.class);
		Fixtures.delete(Account.class);
		Fixtures.loadModels("fixture/goods_base.yml", "fixture/user.yml", 
				"fixture/goods.yml","fixture/accounts.yml",
				"fixture/orders.yml",
				"fixture/orderItems.yml");
	}

	/**
	 * 测试订单列表
	 */
	@Test
	public void queryInfo() {
		String eCouponSn = "003";
		Long supplierId = 1l;
		Map<String, Object> map = ECoupon.queryInfo(eCouponSn, supplierId);
		assertEquals(0, map.size());

		eCouponSn = "002";
		map = ECoupon.queryInfo(eCouponSn, supplierId);
		assertEquals("哈根达斯200元抵用券", map.get("name"));
	}

	@Test
	public void testUpdate() {
		String eCouponSn = "002";
		Long supplierId = 1l;
        ECoupon eCoupon = ECoupon.query(eCouponSn, supplierId);
        assertNotNull(eCoupon);
        eCoupon.consumed();
		List<ECoupon> couponList= ECoupon.find("byECouponSn", eCouponSn).fetch();
		assertEquals(couponList.get(0).status ,ECouponStatus.CONSUMED);
	}



	/**
	 * 测试券列表
	 */
	@Test
	public void testQueryCoupons() {
		Long supplierId = 1l;
		int pageNumber = 1;
		int pageSize = 15;
		List<ECoupon> list = ECoupon.queryCoupons(supplierId, pageNumber, pageSize);
		assertEquals(2, list.size());

	}
	
	/**
	 * 测试用户中心券列表
	 */
	@Test
	public void testUserQueryCoupons(){
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
