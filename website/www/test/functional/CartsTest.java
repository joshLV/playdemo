/**
 *
 */
package functional;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.consumer.User;
import models.consumer.UserInfo;
import models.order.Cart;
import models.sales.Goods;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Http.Response;
import play.test.FunctionalTest;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    //    @Test
    public void testIndexIsBuyFlag() {
        auth();
        Response response = GET("/carts");
        assertStatus(200, response);
        assertContentMatch("一百券 - 购物车", response);
        assertEquals(user, renderArgs("user"));
        assertEquals(1, ((List<Cart>) renderArgs("carts")).size());
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
    public void testOrderUserAndCookiesNull() {
        Security.cleanLoginUserForTest();
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
    public void testOrderUserNormal() {
        Map<String, String> orderParams = new HashMap<>();
        cart.cookieIdentity = "abcde";
        cart.number = 2;
        cart.save();
        Map<String, Http.Cookie> passCookie = new HashMap();
        Http.Cookie newCookie = new Http.Cookie();
        newCookie.name = "identity";
        newCookie.value = "abcdef";
        passCookie.put("identity", newCookie);
        Http.Request request = FunctionalTest.newRequest();
        request.cookies = passCookie;
        orderParams.put("goodsId", String.valueOf(goods.id));
        orderParams.put("increment", "1");
        Response response = POST(request, "/carts", orderParams, new HashMap<String, File>());
        assertStatus(200, response);
        assertContentType("application/json", response);
        assertEquals("{\"count\":1, \"amount\":\"8.50\"}", response.out.toString());
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

    @Test
    public void testTops_cartsIsLess5() {
        auth();
        Map<String, Http.Cookie> passCookie = new HashMap();
        Http.Cookie newCookie = new Http.Cookie();
        newCookie.name = "identity";
        newCookie.value = "abcdef";
        passCookie.put("identity", newCookie);
        Http.Request request = FunctionalTest.newRequest();
        request.cookies = passCookie;

        Response response = GET("/carts/tops");
        assertStatus(200, response);
        assertEquals(1, renderArgs("count"));
        List<Cart> cartList = (List) renderArgs("carts");
        assertEquals(1, cartList.size());
    }

    @Test
    public void testTops_cartsIsMoreThan5() {
        auth();
        Map<String, Http.Cookie> passCookie = new HashMap();
        Http.Cookie newCookie = new Http.Cookie();
        newCookie.name = "identity";
        newCookie.value = "abcdef";
        passCookie.put("identity", newCookie);
        final Http.Request request = FunctionalTest.newRequest();
        request.cookies = passCookie;
        List<Goods> goodsList = FactoryBoy.batchCreate(6, Goods.class,
                new SequenceCallback<Goods>() {
                    @Override
                    public void sequence(Goods target, int seq) {
                    }
                });
        List<Cart> carts = FactoryBoy.batchCreate(6, Cart.class,
                new SequenceCallback<Cart>() {
                    @Override
                    public void sequence(Cart target, int seq) {
                        cart.cookieIdentity = "abcdef";
                    }
                });

        int i = 0;
        for (Cart cart1 : carts) {
            cart1.number = 1l;
            cart1.goods = goodsList.get(i++);
            cart1.save();
        }
        Response response = GET("/carts/tops");
        assertStatus(200, response);
        assertEquals(7, renderArgs("count"));
        List<Cart> cartList = (List) renderArgs("carts");
        assertEquals(7, cartList.size());
    }

    private void auth() {
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

    }


}
