package unit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import models.accounts.Account;
import models.accounts.AccountType;
import models.consumer.User;
import models.order.CouponsCondition;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Area;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;

import org.junit.Before;
import org.junit.Test;

import play.modules.paginate.JPAExtPaginator;
import play.test.Fixtures;
import play.test.UnitTest;

public class CouponsUnitTest extends UnitTest {
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
		Fixtures.loadModels("fixture/goods_base.yml","fixture/user.yml", 
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
		CouponsCondition condition = new CouponsCondition();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			condition.createdAtBegin = sdf.parse("2012-03-01");
			condition.createdAtEnd = new Date();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		condition.status = ECouponStatus.UNCONSUMED;
		condition.goodsName = "";
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
		int pageNumber =1;
		int pageSize =15;
		JPAExtPaginator<ECoupon> list = ECoupon.userCouponsQuery(condition,userId, AccountType.CONSUMER,pageNumber, pageSize);
		assertEquals(0,list.size());

	}

	/**
	 * 测试用户中心券列表
	 */
	@Test
	public void testECoupon(){
		Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
		Goods goods = Goods.findById(goodsId);
		Long orderId = (Long) Fixtures.idCache.get("models.order.Order-order1");
		Order order = Order.findById(orderId);
		ECoupon coupon = null;
		for (OrderItems orderItem:order.orderItems) {
			coupon = new ECoupon(order,goods,orderItem);
		}
		assertNotNull(coupon);
	}
}
