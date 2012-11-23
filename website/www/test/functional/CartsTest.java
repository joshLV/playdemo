/**
 *
 */
package functional;

import java.util.HashMap;
import java.util.Map;

import models.consumer.User;
import models.consumer.UserInfo;
import models.order.Cart;
import models.sales.Goods;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.FunctionalTest;
import controllers.modules.website.cas.Security;
import factory.FactoryBoy;

/**
 * @author wangjia
 * @date 2012-7-31 上午10:42:21
 */
public class CartsTest extends FunctionalTest {
    UserInfo userInfo;
    User user;
    Goods goods;
    Cart cart;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        userInfo = FactoryBoy.create(UserInfo.class);
        user = FactoryBoy.create(User.class);
        goods = FactoryBoy.create(Goods.class);
        cart = FactoryBoy.create(Cart.class);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testIndexIsBuyFlag() {
        auth();
        Response response = GET("/carts");
        assertStatus(200, response);
        assertContentMatch("一百券 - 购物车", response);
        assertEquals(user, renderArgs("user"));
    }

    @Test
    public void testOrderGoodsNull() {
        auth();
        Map<String, String> orderParams = new HashMap<>();
        orderParams.put("goodsId", String.valueOf(999));
        orderParams.put("increment", "1");
        Response response = POST("/carts", orderParams);
        assertStatus(500, response);
        assertContentMatch("no such goods", response);

    }

    @Test
    public void testOrderUserNull() {
        Map<String, String> orderParams = new HashMap<>();
        cart.number = 2;
        cart.save();
        orderParams.put("goodsId", String.valueOf(goods.id));
        orderParams.put("increment", "1");
        Response response = POST("/carts", orderParams);
        assertStatus(500, response);
        assertContentMatch("can not identity current user", response);
    }

    @Test
    public void testDeleteUserNull() {
        Response response = DELETE("/carts/" + goods.id);
        assertStatus(500, response);
        assertContentMatch("can not identity current user", response);
    }

    @Test
    public void testDeleteGoodsIdValid() {
        auth();
        String goodsId = null;
        Response response = DELETE("/carts/" + goodsId);
        assertStatus(500, response);
        assertContentMatch("no goods specified", response);
    }

    @Test
    public void testDeleteGoods() {
        auth();
        Cart testCart;
        testCart = Cart.find("id=?", cart.id).first();
        assertNotNull(testCart);
        assertEquals(1, Cart.count());

        Response response = DELETE("/carts/" + goods.id);
        assertStatus(200, response);
        testCart = Cart.find("id=?", cart.id).first();
        assertNull(testCart);
        assertEquals(0, Cart.count());

    }

    private void auth() {
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

    }


}
