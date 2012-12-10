package functional;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import models.accounts.CashCoupon;
import models.consumer.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.cache.Cache;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;

/**
 * Test UserCashCoupons
 * <p/>
 * User: wangjia
 * Date: 12-12-10
 * Time: 上午9:21
 */
public class UserCashCouponsTest extends FunctionalTest {
    User user;
    CashCoupon cashCoupon;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        user = FactoryBoy.create(User.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        cashCoupon = FactoryBoy.create(CashCoupon.class);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/cash-coupon");
        assertIsOk(response);
        assertEquals("verify", renderArgs("action"));
    }

    @Test
    public void testVerify() {
        Cache.set("randomID", "111", "30mn");
        Map<String, String> params = new HashMap<>();
        params.put("couponCode", cashCoupon.chargeCode);
        params.put("code", "111");
        params.put("randomID", "randomID");
        Http.Response response = POST("/cash-coupon/verify", params);
        assertIsOk(response);
        assertEquals(cashCoupon.chargeCode, renderArgs("couponCode"));
        assertEquals("use", renderArgs("action"));
    }

    @Test
    public void testUseCoupon() {
        assertNull(cashCoupon.chargedAt);
        Cache.set("ridA", "ridB", "30000000mn");
        Cache.set("ridB", cashCoupon.id, "30000000mn");
        Map<String, String> params = new HashMap<>();
        params.put("ridA", "ridA");
        params.put("ridB", "ridB");
        Http.Response response = POST("/cash-coupon/use", params);
        assertIsOk(response);
        cashCoupon.refresh();
        assertNotNull(cashCoupon.chargedAt);
        assertEquals("充值成功", renderArgs("suc"));

    }
}
