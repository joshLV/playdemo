package functional;

import controllers.modules.website.cas.Security;
import models.consumer.User;
import models.order.PointGoodsOrder;
import models.sales.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-15
 * Time: 下午4:16
 * To change this template use File | Settings | File Templates.
 */
public class PGPaymentInfoFuncTest extends FunctionalTest {

    @Before
    public void setUp(){
        Fixtures.delete(PointGoods.class);
        Fixtures.delete(Shop.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(Category.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(Area.class);
        Fixtures.delete(PointGoodsOrder.class);
        Fixtures.loadModels("Fixture/pointgoods.yml");
        Fixtures.loadModels("Fixture/areas_unit.yml");
        Fixtures.loadModels("Fixture/categories_unit.yml");
        Fixtures.loadModels("Fixture/supplier_unit.yml");
        Fixtures.loadModels("Fixture/brands_unit.yml");
        Fixtures.loadModels("Fixture/shops_unit.yml");
        Fixtures.loadModels("Fixture/goods_unit.yml");
        Fixtures.loadModels("Fixture/user.yml");
        Fixtures.loadModels("Fixture/userInfo.yml");
        Fixtures.loadModels("Fixture/pointgoodsorder.yml");

        Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
        User user = User.findById(userId);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testIndex(){

        Long pointGoodsId =(Long) Fixtures.idCache.get("models.sales.PointGoods-pointgoods1");
        assertNotNull(pointGoodsId);
        HashMap<String,String> params = new HashMap<>();
        params.put("gid",pointGoodsId.toString());
        params.put("number","1");
        params.put("mobile","13512345678");
        params.put("remark","");

        Http.Response response = POST("/payment_info/index",params);
        assertIsOk(response);
        assertContentMatch("一百券积分礼品兑换",response);

    }

    @Test
    public void testCreate(){

        Long pointGoodsId =(Long) Fixtures.idCache.get("models.sales.PointGoods-pointgoods1");
        assertNotNull(pointGoodsId);
        HashMap<String,String> params = new HashMap<>();
        params.put("goodsId",pointGoodsId.toString());
        params.put("number","1");
        params.put("mobile","13512345678");
        params.put("remark","123456");

        Http.Response response = POST("/payment_info/confirm",params);
        assertStatus(302,response);

    }

    @Test
    public void testSuccess(){
        Http.Response response = GET("/payment_info/1213231412");
        assertIsOk(response);
        assertContentMatch("我的积分",response);
    }
}
