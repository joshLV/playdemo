package functional;

import java.util.List;

import factory.callback.BuildCallback;
import models.consumer.User;
import models.consumer.UserInfo;
import models.sales.Category;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;

import org.junit.Before;
import org.junit.Test;

import play.i18n.Messages;
import play.mvc.Http;
import play.test.FunctionalTest;
import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import util.DateHelper;

/**
 * User: wangjia
 * Date: 12-9-29
 * Time: 上午11:06
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
        goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.beginOnSaleAt = com.uhuila.common.util.DateUtil.getEndOfDay(DateHelper.beforeDays(g.effectiveAt, 5));
                g.endOnSaleAt = com.uhuila.common.util.DateUtil.getEndOfDay(DateHelper.beforeDays(g.expireAt, 5));
            }
        });
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @Test
    public void testTuan800() {
        Messages.defaults.put("tuan800category." + category.id, "餐饮美食");
        Http.Response response = GET("/feed/tuan800");
        assertStatus(200, response);
        assertContentMatch("Product Name", response);
        List<models.sales.Goods> goodsList = (List<Goods>) renderArgs("goodsList");
        assertEquals(1, goodsList.size());
        assertEquals(goods.name, goodsList.get(0).name);
    }

    @Test
    public void testTuan360() {
        Messages.defaults.put("tuan360category." + goods.getCategories().iterator().next().id, "餐饮美食");
        Http.Response response = GET("/feed/tuan360");
        assertStatus(200, response);
        assertContentMatch("Product Title", response);
        List<models.sales.Goods> goodsList = (List<Goods>) renderArgs("goodsList");
        assertEquals(1, goodsList.size());
        assertEquals(goods.title, goodsList.get(0).title);
    }


    @Test
    public void testTuanBaidu() {
        Messages.defaults.put("tuanBaiduCategory1." + goods.getCategories().iterator().next().id, "餐饮美食");
        Messages.defaults.put("tuanBaiduCategory2." + goods.getCategories().iterator().next().id, "地方菜");
        Http.Response response = GET("/feed/tuanbaidu");
        assertStatus(200, response);
        assertContentMatch("Product Name", response);
        List<models.sales.Goods> goodsList = (List<Goods>) renderArgs("goodsList");
        assertEquals(1, goodsList.size());
        assertEquals(goods.name, goodsList.get(0).name);
    }

    @Test
    public void testTuanLing() {
        Messages.defaults.put("tuanLingCategory." + category.id, "餐饮美食");
        Http.Response response = GET("/feed/tuanling");
        assertStatus(200, response);
        assertContentMatch("Product Title", response);
        List<models.sales.Goods> goodsList = (List<Goods>) renderArgs("goodsList");
        assertEquals(1, goodsList.size());
        assertEquals(goods.title, goodsList.get(0).title);
    }

}
