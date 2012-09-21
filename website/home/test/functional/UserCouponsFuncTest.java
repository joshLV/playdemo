package functional;

import controllers.modules.website.cas.Security;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.consumer.User;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Juno
 * Date: 12-7-30
 * Time: 下午2:12
 * To change this template use File | Settings | File Templates.
 */
public class UserCouponsFuncTest extends FunctionalTest {

    @Before
    public void setup() {
        Fixtures.delete(User.class);
        Fixtures.delete(Order.class);
        Fixtures.delete(OrderItems.class);

        Fixtures.delete(ECoupon.class);

        //Fixtures.loadModels("fixture/user.yml", "fixture/userInfo.yml");
        Fixtures.loadModels("fixture/user.yml");
        Fixtures.loadModels("fixture/userInfo.yml");
        Fixtures.loadModels("fixture/supplier_unit.yml");
        Fixtures.loadModels("fixture/brands.yml");
        Fixtures.loadModels("fixture/categories_unit.yml");
        Fixtures.loadModels("fixture/shops.yml");
        Fixtures.loadModels("fixture/goods.yml");
        Fixtures.loadModels("fixture/carts.yml");
        //Fixtures.loadModels("fixture/goods_base.yml");
        Fixtures.loadModels("fixture/orders.yml");
        Fixtures.loadModels("fixture/orderItems.yml");
        Fixtures.loadModels("fixture/ecoupon.yml");

        Long userId = (Long) Fixtures.idCache.get("models.consumer.User-selenium");
        User user = User.findById(userId);

        //设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testApplyRefund() {
        // 设置总账户中的余额
        Account account = AccountUtil.getPlatformIncomingAccount();
        account.amount = new BigDecimal("100000000");
        account.save();

        long id = (Long) Fixtures.idCache.get("models.order.ECoupon-ecoupon_001");
        assertNotNull(id);
        // 设置 userId, 因为在applyRefund 方法中有比较
        ECoupon eCoupon = ECoupon.findById(id);
        eCoupon.order.userId = (Long) Fixtures.idCache.get("models.consumer.User-selenium");
        eCoupon.order.userType = AccountType.CONSUMER;
        // 保存修改
        eCoupon.order.save();


        Map<String, String> params = new HashMap<>();
        params.put("applyNote", "我要退款");

        Http.Response response = POST("/coupons/refund/" + id, params);
        assertIsOk(response);
        eCoupon = ECoupon.findById(id);
        eCoupon.refresh();
        assertEquals(ECouponStatus.REFUND, eCoupon.status);
    }

    @Test
    public void testSendMessage() {
        long id = (Long) Fixtures.idCache.get("models.order.ECoupon-ecoupon_001");
        Http.Response response = GET("/coupons-message/" + id + "/send");
        assertIsOk(response);
    }

}
