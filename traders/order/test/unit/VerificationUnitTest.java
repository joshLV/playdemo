package unit;

import java.util.List;
import java.util.Map;

import models.order.ECoupon;
import models.order.Orders;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import play.Play;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.UnitTest;

public class VerificationUnitTest extends UnitTest {
	 @Before
	    @SuppressWarnings("unchecked")
	    public void setup() {
	        Fixtures.delete(models.order.Orders.class);
	        Fixtures.delete(models.sales.Goods.class);
	        Fixtures.delete(models.consumer.User.class);
	        Fixtures.loadModels("fixture/goods_base.yml","fixture/goods.yml","fixture/orders.yml","fixture/user.yml");
	    }

	    /**
	     * 测试订单列表
	     */
		@Test
		public void queryInfo(){
			ECoupon eCoupon=new ECoupon();
			String eCouponSn="002";
			Long compnayId=1l;
			Map<String,Object> map = eCoupon.queryInfo(eCouponSn,compnayId);
			Assert.assertEquals("002",map.get("eCouponSn"));  

		}

}
