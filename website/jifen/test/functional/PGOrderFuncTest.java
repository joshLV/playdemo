package functional;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import models.consumer.User;
import models.order.PointGoodsOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

/**
 * 积分商城订单的功能测试.
 *
 * User: hejun
 * Date: 12-8-15
 * Time: 下午4:00
 */
public class PGOrderFuncTest extends FunctionalTest {
    PointGoodsOrder pointGoodsOrder;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        User user = FactoryBoy.create(User.class);

        pointGoodsOrder = FactoryBoy.create(PointGoodsOrder.class);
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
        Http.Response response = GET("/orders?g" + pointGoodsOrder.pointGoods.id + "=1&gid=" + pointGoodsOrder.pointGoods.id);
        assertIsOk(response);
        assertContentMatch("核对订单信息", response);
    }

}
