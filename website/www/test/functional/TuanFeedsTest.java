package functional;

import controllers.TuanFeeds;
import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.callback.SequenceCallback;
import models.consumer.Address;
import models.consumer.User;
import models.consumer.UserInfo;
import models.sales.Category;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.i18n.Messages;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.util.List;
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
    Supplier supplier;
    Category category;
    Shop shop;
    Goods goods;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        userInfo = FactoryBoy.create(UserInfo.class);
        user = FactoryBoy.create(User.class);
        supplier = FactoryBoy.create(Supplier.class);
        category = FactoryBoy.create(Category.class);
        shop = FactoryBoy.create(Shop.class);
        goods = FactoryBoy.create(Goods.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @Test
    public void testTuan800() {
        System.out.println("category.id>>>>>"+category.id);
        Messages.defaults.put("tuan800category." + category.id,"餐饮美食");
        Http.Response response = GET("/feed/tuan800");
        assertStatus(200, response);
        assertContentMatch("Product Name", response);
    }

    @Test
    public void testTuan360() {
        Messages.defaults.put("tuan360category." + goods.getCategories().iterator().next().id,"餐饮美食");
        Http.Response response = GET("/feed/tuan360");
        assertStatus(200, response);
        assertContentMatch("Product Title", response);
    }


    @Test
    public void testTuanBaidu() {
        Messages.defaults.put("tuanBaiduCategory1." + goods.getCategories().iterator().next().id,"餐饮美食");
        Messages.defaults.put("tuanBaiduCategory2." + goods.getCategories().iterator().next().id,"地方菜");
        Http.Response response = GET("/feed/tuanBaidu");
        assertStatus(200, response);
        assertContentMatch("Product Name", response);
    }

}
