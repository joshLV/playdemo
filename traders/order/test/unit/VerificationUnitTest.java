package unit;

import java.util.Date;
import java.util.List;
import java.util.Map;

import models.accounts.Account;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.consumer.User;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Area;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.supplier.Supplier;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import play.modules.paginate.JPAExtPaginator;
import play.test.Fixtures;
import play.test.UnitTest;

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
		Fixtures.delete(SupplierRole.class);
		Fixtures.delete(Supplier.class);
		Fixtures.delete(SupplierUser.class);
		Fixtures.loadModels("fixture/goods_base.yml", "fixture/roles.yml", 
				"fixture/supplier_users.yml",
				"fixture/user.yml",
				"fixture/goods.yml","fixture/accounts.yml",
				"fixture/orders.yml",
				"fixture/orderItems.yml");

		Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc");
		Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_002");
		Goods goods = Goods.findById(goodsId);
		goods.supplierId = supplierId;
		goods.save();
		goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
		goods = Goods.findById(goodsId);
		goods.supplierId = supplierId;
		goods.save();
	}

	/**
	 * 测试订单列表
	 */
	@Test
	public void queryInfo() {
		String eCouponSn = "003";
		Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc");
		Map<String, Object> map = ECoupon.queryInfo(eCouponSn, supplierId);
		assertEquals(0, map.size());

		eCouponSn = "002";
		map = ECoupon.queryInfo(eCouponSn, supplierId);
		assertEquals("哈根达斯200元抵用券", map.get("name"));
	}

	@Test
	public void testUpdate() {
		String eCouponSn = "002";
		Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc");
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
		Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc");
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
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
		User user = User.findById(userId);
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
