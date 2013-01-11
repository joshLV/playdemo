package functional;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import models.consumer.User;
import models.consumer.UserGoldenCoin;
import models.sales.Goods;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

/**
 * <p/>
 * User: yanjy
 * Date: 13-1-11
 * Time: 下午6:03
 */
public class ObtainGoldenCoinsTest extends FunctionalTest {
    User user;
    UserGoldenCoin coin;
    Goods goods;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        goods = FactoryBoy.create(Goods.class);
        user = FactoryBoy.create(User.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @Test
    public void testIndex() {
        assertEquals(0, UserGoldenCoin.count());
        Http.Response response = GET("/coins?gid=" + goods.id);
        assertStatus(302, response);
        assertEquals(1, UserGoldenCoin.count());
        coin = UserGoldenCoin.find("order by id desc").first();
        assertEquals(user, coin.user);
        assertEquals("每天签到", coin.remarks);
    }

    @Test
    public void testIndex_noGoods() {
        assertEquals(0, UserGoldenCoin.count());
        Http.Response response = GET("/coins?gid=9999");
        assertContentMatch("很抱歉，系统发生错误", response);
    }
}
