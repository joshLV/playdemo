package functional;

import models.consumer.User;
import models.consumer.UserInfo;
import models.sales.SecKillGoodsItem;

import org.junit.Before;
import org.junit.Test;

import play.mvc.Http;
import play.test.FunctionalTest;
import controllers.modules.website.cas.Security;
import factory.FactoryBoy;


/**
 * User: wangjia
 * Date: 12-8-22
 * Time: 上午11:31
 */
public class SecKillGoodsControllerTest extends FunctionalTest {

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        UserInfo userInfo = FactoryBoy.create(UserInfo.class);
        User user = FactoryBoy.create(User.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @Test
    public void testIndex() {
        SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class);
        Http.Response response = GET("/seckill-goods");
        assertStatus(200, response);
        assertContentType("text/html", response);
        assertContentMatch("秒杀", response);
    }

    @Test
    public void testIndexNoGoods() {
        Http.Response response = GET("/seckill-goods");
        assertStatus(302, response);
    }


}
