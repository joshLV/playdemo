package unit;

import java.util.Date;

import javax.persistence.OptimisticLockException;

import models.accounts.Account;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.consumer.User;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Area;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.supplier.Supplier;

import org.junit.Before;
import org.junit.Test;

import play.db.jpa.JPAPlugin;
import play.test.Fixtures;
import play.test.UnitTest;

public class EcouponLockVersionTest extends UnitTest{

	@Before
	public void setup() {
		Fixtures.delete(Account.class);
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

	@Test
	public void testWithLockAndRetry() throws Exception{

		Thread a = new Thread(){

			public void run(){  
				JPAPlugin.startTx(false);
				String eCouponSn = "1234567002";
				Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc");
				ECoupon eCoupon = ECoupon.query(eCouponSn, supplierId);
				try {
					sleep(100l);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				boolean flag = false;
				try {
					eCoupon.consumedAt = new Date();
					eCoupon.save();
				} catch(OptimisticLockException e) {
					flag = true;
				}
				assertFalse("Found Lock check.", flag);
			}
		};        

		String eCouponSn = "1234567002";
		Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc");
		ECoupon eCoupon = ECoupon.query(eCouponSn, supplierId);
		a.start();
		
		Thread.sleep(300l);
		boolean flag = false;
		try {
			eCoupon.consumedAt = new Date();
			eCoupon.save();
		} catch(OptimisticLockException e) {
			flag = true;
		}

		assertTrue("Not Found Lock check.", flag);
	}

}
