package functional;

import controllers.UserCarts;
import controllers.modules.website.cas.Security;
import models.consumer.User;
import models.order.Cart;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Juno
 * Date: 12-7-27
 * Time: 下午1:49
 * To change this template use File | Settings | File Templates.
 */
public class UserCartsFunctionalTest extends FunctionalTest {

    @Before
    public void setup() {
        Fixtures.delete(User.class);

        //Fixtures.loadModels("fixture/user.yml", "fixture/userInfo.yml");
        Fixtures.loadModels("fixture/user.yml");
        Fixtures.loadModels("fixture/userInfo.yml");
        Fixtures.loadModels("fixture/supplier_unit.yml");
        Fixtures.loadModels("fixture/brands.yml");
        Fixtures.loadModels("fixture/categories_unit.yml");
        Fixtures.loadModels("fixture/shops.yml");
        Fixtures.loadModels("fixture/goods.yml");
        Fixtures.loadModels("fixture/carts.yml");


        Long userId= (Long) Fixtures.idCache.get("models.consumer.User-selenium");
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
    public void testTops(){

        Http.Response response = GET("/carts/tops");
        assertIsOk(response);
        assertContentMatch("哈根达斯", response);

    }

    @Test
    public void testDelete(){

        long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        Long userId = (Long) Fixtures.idCache.get("models.consumer.User-selenium");
        User user = User.findById(userId);
        String cookieValue = "";
        List<Cart> cartList = Cart.findAll(user, cookieValue);
        int oldSize = cartList.size();

        Http.Response response = DELETE("/carts/"+goodsId);
        cartList = Cart.findAll(user,cookieValue);
        int newSize = cartList.size();
        assertIsOk(response);
        //删除成功， 购物车中商品数减一。
        assertEquals(oldSize-1,newSize);

    }

    public void testDeleteNull(){

    }
}
