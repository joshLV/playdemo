package functional;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import models.consumer.Address;
import models.consumer.User;
import models.consumer.UserInfo;
import models.sales.Category;
import models.sales.Goods;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-9-29
 * Time: 上午11:06
 * To change this template use File | Settings | File Templates.
 */
public class TuanFeedsTest extends FunctionalTest {
    UserInfo userInfo;
    User user;
    Category category;
    Set<Category> categories;
    Goods goods;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        userInfo = FactoryBoy.create(UserInfo.class);
        user = FactoryBoy.create(User.class);

        category =  FactoryBoy.create(Category.class);


        goods = FactoryBoy.create(Goods.class);

        goods.categories.iterator().next().id = 1l;
        goods.save();

        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @Test
    public void testTuan800() {
        Http.Response response = GET("/feed/tuan800");
        assertStatus(200, response);


    }


}
