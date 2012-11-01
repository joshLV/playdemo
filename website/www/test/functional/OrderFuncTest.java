package functional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.consumer.User;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Area;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;
import controllers.modules.website.cas.Security;
import factory.FactoryBoy;

/**
 * 订单的功能测试.
 * <p/>
 * User: hejun
 * Date: 12-8-22
 * Time: 下午2:17
 */
public class OrderFuncTest extends FunctionalTest {

    User user;

    @Before
    public void setup() {
        FactoryBoy.lazyDelete();
        Fixtures.delete(Order.class);
        Fixtures.delete(User.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(OrderItems.class);
        Fixtures.delete(Order.class);
        Fixtures.delete(Shop.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(Category.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(Area.class);
        Fixtures.delete(Supplier.class);

        Fixtures.loadModels("fixture/user.yml");
        Fixtures.loadModels("fixture/supplier_unit.yml");
        Fixtures.loadModels("fixture/areas_unit.yml");
        Fixtures.loadModels("fixture/categories_unit.yml");
        Fixtures.loadModels("fixture/brands_unit.yml");
        Fixtures.loadModels("fixture/shops_unit.yml");
        Fixtures.loadModels("fixture/goods_unit.yml");
        Fixtures.loadModels("fixture/orders.yml");

        //设置虚拟登陆
        Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
        user = User.findById(userId);


        user = FactoryBoy.create(User.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @Test
    public void testIndex_无参数() {
        Http.Response response = GET("/orders");
        assertStatus(500, response);
    }

    // FIXME
    @Ignore
    @Test
    public void testIndex_有参数() {
//        Goods goodsA = FactoryBoy.create(Goods.class);
//        OrderItems orderItem = FactoryBoy.create(OrderItems.class);
//        orderItem.order.userId = user.id;
//        orderItem.order.save();
//        Goods goodsB = FactoryBoy.create(Goods.class);
//        orderItem = FactoryBoy.create(OrderItems.class);
//        orderItem.order.userId = user.id;
//        orderItem.save();

        Long goodsId1 = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        Long goodsId2 = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_002");
        Long orderId = (Long) Fixtures.idCache.get("models.order.Order-order_unpaid");
        Order order = Order.findById(orderId);
        order.userId = user.id;
        order.save();

        Http.Response response = GET("/orders?gid=" + goodsId1 + "&g" + goodsId1 + "=1&gid=" + goodsId2 + "&g" + goodsId2 + "=1");
//        Http.Response response = GET("/orders?gid=" + goodsA.id + "&g" + goodsA.id + "=1&gid=" + goodsB.id + "&g" + goodsB.id + "=1");
        assertStatus(200, response);

        String querystring = (String) renderArgs("querystring");
        User resultUser = (User) renderArgs("user");
        assertNotNull(resultUser);
        assertTrue(querystring.contains("gid=" + goodsId1));
        assertTrue(querystring.contains("gid=" + goodsId2));
        assertTrue(querystring.contains("g" + goodsId1 + "=1"));
        assertTrue(querystring.contains("g" + goodsId2 + "=1"));
        assertEquals(user.id, resultUser.id);

        List<String> orderItems_mobiles = (List<String>) renderArgs("orderItems_mobiles");
        assertEquals(1, orderItems_mobiles.size());
        assertEquals("15312345674", orderItems_mobiles.get(0));
    }


    @Test
    public void testCreate() {
        Map<String, String> params = new HashMap<>();
        Long goodsId1 = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        Long goodsId2 = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_002");
        String gids = goodsId1.toString() + "-3," + goodsId2.toString() + "-2,";

        int orderCount = Order.findAll().size();
        params.put("items", gids);
        params.put("mobile", "13800001111");
        params.put("remark", "hehe");
        //Please fix me

//        Http.Response response = POST("/orders/new", params);
//        assertStatus(302, response);

//        int resultOrderCount = Order.findAll().size();
//        assertEquals(orderCount + 1, resultOrderCount);
    }

    @Test
    public void testCheckLimitNumber_商品未限制购买数() {
        Long goodsId1 = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        Long goodsId2 = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_002");
        String gids = goodsId1.toString() + "-3," + goodsId2.toString() + "-2";

        Http.Response response = GET("/orders_number?items=" + gids);
        assertStatus(200, response);

        assertEquals("0", response.out.toString());
    }

    @Test
    public void testCheckLimitNumber_商品限制购买数() {
        Long goodsId1 = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        Long goodsId2 = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_002");
        String gids = goodsId1.toString() + "-3," + goodsId2.toString() + "-2";

        Goods goods1 = Goods.findById(goodsId1);
        goods1.limitNumber = 1;
        goods1.save();

        Http.Response response = GET("/orders_number?items=" + gids);
        assertStatus(200, response);

        assertEquals("1", response.out.toString());
    }

}
