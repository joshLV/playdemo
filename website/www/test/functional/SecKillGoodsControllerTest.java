package functional;

import controllers.modules.website.cas.Security;
import models.consumer.User;
import models.sales.SecKillGoodsItem;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import factory.FactoryBoy;


/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-8-22
 * Time: 上午11:31
 * To change this template use File | Settings | File Templates.
 */
public class SecKillGoodsControllerTest extends FunctionalTest {

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();

        User user = FactoryBoy.create(User.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @Test
    public void testIndex() {
        SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class);
        Http.Response response = GET("/seckill-goods");
    }


}
