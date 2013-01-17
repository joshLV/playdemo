package functional;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import models.consumer.User;
import models.consumer.UserInfo;
import models.order.PointGoodsOrder;
import models.sales.PointGoods;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.util.HashMap;

/**
 * User: hejun
 * Date: 12-8-15
 * Time: 下午4:16
 */
public class PGPaymentInfoFuncTest extends FunctionalTest {
    User user;
    PointGoods pointGoods;
    PointGoodsOrder pointOrders;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        user = FactoryBoy.create(User.class);
        UserInfo userInfo = FactoryBoy.create(UserInfo.class);
        userInfo.user = user;
        userInfo.save();
        pointGoods = FactoryBoy.create(PointGoods.class);
        pointOrders = FactoryBoy.create(PointGoodsOrder.class);
        pointOrders.userId = user.id;
        pointOrders.save();
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testIndex() {

        HashMap<String, String> params = new HashMap<>();
        params.put("gid", pointGoods.id.toString());
        params.put("number", "1");
        params.put("mobile", "13512345678");
        params.put("remark", "");

        Http.Response response = POST("/payment_info/index", params);
        assertIsOk(response);
        assertContentMatch("一百券积分礼品兑换", response);

    }

    @Test
    public void testCreate() {
        HashMap<String, String> params = new HashMap<>();
        params.put("goodsId", pointGoods.id.toString());
        params.put("number", "1");
        params.put("mobile", "13512345678");
        params.put("remark", "123456");

        Http.Response response = POST("/payment_info/confirm", params);
        assertStatus(302, response);

    }

    @Test
    public void testSuccess() {
        Http.Response response = GET("/payment_info/" +pointOrders.orderNumber);
        assertIsOk(response);
        assertContentMatch("我的积分", response);
    }
}
