package unit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import models.accounts.Account;
import models.accounts.AccountType;
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
import models.sales.Shop;
import models.supplier.Supplier;
import navigation.RbacLoader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.test.Fixtures;
import play.test.UnitTest;
import play.vfs.VirtualFile;
import controllers.supplier.cas.Security;

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
				"fixture/supplierusers.yml",
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

	@After
	public void tearDown() {
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);  
	}
	/**
	 * 测试订单列表
	 */
	@Test
	public void queryInfo() {
		String eCouponSn = "003";
		Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc");
		Long shopId = (Long) Fixtures.idCache.get("models.sales.Shop-Shop_4");
		Map<String, Object> map = ECoupon.queryInfo(eCouponSn, supplierId,shopId);
		assertEquals(0, map.size());

		eCouponSn = "1234567002";
		Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_002");
		Goods goods = Goods.findById(goodsId);
		goods.useBeginTime="00:00";
		goods.useEndTime="23:50";
		map = ECoupon.queryInfo(eCouponSn, supplierId,shopId);
		assertEquals("哈根达斯200元抵用券", map.get("name"));
		assertEquals(0, map.get("error"));
		
		eCouponSn = "1234567003";
		goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
		goods = Goods.findById(goodsId);
		goods.useBeginTime="19:00";
		goods.useEndTime="20:00";
		goods.save();
		shopId = (Long) Fixtures.idCache.get("models.sales.Shop-Shop_5");
		map = ECoupon.queryInfo(eCouponSn, supplierId,shopId);
		assertEquals(2, map.get("error"));
		
		eCouponSn = "1234567002";
		shopId = (Long) Fixtures.idCache.get("models.sales.Shop-Shop_6");
		map = ECoupon.queryInfo(eCouponSn, supplierId,shopId);
		assertEquals(1, map.get("error"));
	}

	/**
	 * 测试用户中心券列表
	 */
	@Test
	public void testGetTimeRegion(){
		SimpleDateFormat time = new SimpleDateFormat( "HH:mm:ss" );
		Date d = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		calendar.add(calendar.HOUR, -1);
		
		Calendar calendar1 = Calendar.getInstance();
		calendar1.setTime(d);
		calendar1.add(calendar1.HOUR, 1);
		
		String timeBegin = time.format(calendar.getTime());
		String timeEnd = time.format(calendar1.getTime());
		boolean timeFlag = ECoupon.getTimeRegion(timeBegin,timeEnd);
		assertTrue(timeFlag);
	
		Calendar calendar3 = Calendar.getInstance();
		calendar3.setTime(d);
		calendar3.add(calendar3.HOUR, 1);
		
		Calendar calendar4 = Calendar.getInstance();
		calendar4.setTime(d);
		calendar4.add(calendar4.HOUR, 3);
		
		timeBegin = time.format(calendar3.getTime());
		timeEnd = time.format(calendar4.getTime());
		timeFlag = ECoupon.getTimeRegion(timeBegin,timeEnd);
		assertFalse(timeFlag);
		
		
		Calendar calendar5 = Calendar.getInstance();
		calendar5.setTime(d);
		calendar5.add(calendar5.DAY_OF_MONTH, 1);
		
		calendar4 = Calendar.getInstance();
		calendar4.setTime(d);
		calendar4.add(calendar4.HOUR, 3);
		
		timeBegin = time.format(calendar5.getTime());
		timeEnd = time.format(calendar4.getTime());
		timeFlag = ECoupon.getTimeRegion(timeBegin,timeEnd);
		assertFalse(timeFlag);
		
		Calendar calendar6 = Calendar.getInstance();
		calendar6.setTime(d);
		calendar6.add(calendar5.DAY_OF_MONTH, -1);
		
		calendar4 = Calendar.getInstance();
		calendar4.setTime(d);
		calendar4.add(calendar4.HOUR, 3);
		
		timeBegin = time.format(calendar5.getTime());
		timeEnd = time.format(calendar4.getTime());
		timeFlag = ECoupon.getTimeRegion(timeBegin,timeEnd);
		assertFalse(timeFlag);
	}
	
	@Test
	public void testUpdate() {
		String eCouponSn = "1234567002";
		Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc");
		ECoupon eCoupon = ECoupon.query(eCouponSn, supplierId);
		assertNotNull(eCoupon);
		
		Long shopId = (Long) Fixtures.idCache.get("models.sales.Shop-Shop_4");
		eCoupon.consumed(shopId);
		List<ECoupon> couponList= ECoupon.find("byECouponSn", eCouponSn).fetch();
		assertEquals(ECouponStatus.CONSUMED,couponList.get(0).status);
		assertEquals(shopId,couponList.get(0).shop.id );
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
		assertEquals(4, list.size());

	}
	
	@Test
	public void testGetConsumedShop() {
		String eCouponSn = "1234567004";
		Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc");
		ECoupon eCoupon = ECoupon.query(eCouponSn, supplierId);
		String name = eCoupon.getConsumedShop();
		assertEquals("优惠拉", name);

	}
	
}
