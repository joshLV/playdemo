package functional;

import controllers.UserCarts;
import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import models.consumer.User;
import models.order.Cart;
import models.sales.Category;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.util.List;

/**
 * User: hejun
 * Date: 12-7-27
 */
public class UserCartsFunctionalTest extends FunctionalTest {

    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        User user = FactoryBoy.create(User.class);
        FactoryBoy.create(Supplier.class);
        FactoryBoy.create(Category.class);
        FactoryBoy.create(Shop.class);
        FactoryBoy.create(Goods.class);
        FactoryBoy.create(Cart.class);

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
        Goods goods = FactoryBoy.last(Goods.class);
        Http.Response response = GET("/carts/tops");
        assertIsOk(response);
        int count = (Integer)renderArgs("count");
        assertEquals(1, count);
        assertEquals(1, ((List<Cart>)renderArgs("carts")).size());
    }

    @Test
    public void testDelete(){
        User user = FactoryBoy.last(User.class);
        Goods goods = FactoryBoy.last(Goods.class);
        String cookieValue = "";
        List<Cart> cartList = Cart.findAll(user, cookieValue);
        int oldSize = cartList.size();

        Http.Response response = DELETE("/carts/" + goods.id);
        cartList = Cart.findAll(user,cookieValue);
        int newSize = cartList.size();
        assertIsOk(response);
        //删除成功， 购物车中商品数减一。
        assertEquals(oldSize-1,newSize);

    }
}
