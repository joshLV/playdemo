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

import factory.FactoryBoy;

import play.db.jpa.JPAPlugin;
import play.test.Fixtures;
import play.test.UnitTest;

public class EcouponLockVersionTest extends UnitTest{
    
    Supplier supplier;
    Goods goods;
    ECoupon ecoupon;
	@Before
	public void setup() {
	    FactoryBoy.deleteAll();
	    supplier = FactoryBoy.create(Supplier.class);
	    goods = FactoryBoy.create(Goods.class);
	    ecoupon = FactoryBoy.create(ECoupon.class);
	}

	@Test
	public void testWithLockAndRetry() throws Exception{

		Thread a = new Thread(){

			public void run(){  
				JPAPlugin.startTx(false);
				String eCouponSn = ecoupon.eCouponSn;
				ECoupon eCoupon = ECoupon.query(eCouponSn, supplier.id);
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

		String eCouponSn = ecoupon.eCouponSn;

		ECoupon eCoupon = ECoupon.query(eCouponSn, supplier.id);
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
