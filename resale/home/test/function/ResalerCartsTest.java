package function;

import controllers.modules.resale.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.order.Order;
import models.resale.Resaler;
import models.resale.ResalerCart;
import models.resale.ResalerFav;
import models.sales.Goods;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 13-1-7
 */
public class ResalerCartsTest extends FunctionalTest{
    Resaler resaler;
    Goods goods;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        resaler = FactoryBoy.create(Resaler.class);
        Security.setLoginUserForTest(resaler.loginName);
        goods = FactoryBoy.create(Goods.class);
        FactoryBoy.create(ResalerFav.class);
        FactoryBoy.create(ResalerCart.class, new BuildCallback<ResalerCart>() {
            @Override
            public void build(ResalerCart target) {
                target.phone = "13472581853";
            }
        });
        FactoryBoy.create(ResalerCart.class, new BuildCallback<ResalerCart>() {
            @Override
            public void build(ResalerCart target) {
                target.phone = "13472581854";
            }
        });
        FactoryBoy.create(ResalerCart.class, new BuildCallback<ResalerCart>() {
            @Override
            public void build(ResalerCart target) {
                target.goods = FactoryBoy.create(Goods.class);
                target.phone = "13472581855";
            }
        });
    }

    @Test
    public void 测试默认页() {
        Http.Response response = GET(Router.reverse("controllers.ResalerCarts.index"));
        assertIsOk(response);
        List<ResalerFav> favs = (List<ResalerFav>)renderArgs("favs");
        List<List<ResalerCart>> carts = (List<List<ResalerCart>>)renderArgs("carts");
        assertNotNull(favs);
        assertNotNull(carts);
        assertEquals(1, favs.size());
        assertEquals(2, carts.size());
        List<ResalerCart> cartGroupA = carts.get(0);
        List<ResalerCart> cartGroupB = carts.get(1);

        if (cartGroupA.get(0).phone.equals("13472581855")) {
            assertEquals(1, cartGroupA.size());
        }else {
            assertEquals(2, cartGroupA.size());
        }
        //测试按goods分组
        Long goodsId = cartGroupA.get(0).goods.id;
        for(ResalerCart cart : cartGroupA) {
            assertEquals(goodsId, cart.goods.id);
        }
        goodsId = cartGroupB.get(0).goods.id;
        for(ResalerCart cart : cartGroupB) {
            assertEquals(goodsId, cart.goods.id);
        }
    }

    @Test
    public void 测试确认购物车() {
        List<ResalerCart> allCarts = ResalerCart.findAll(resaler);
        assertTrue(allCarts.size() > 0);
        assertEquals(0, Order.count());

        StringBuilder favItems = new StringBuilder();
        for(ResalerCart cart : allCarts) {
            favItems.append(cart.goods.id) .append("-")
                    .append(cart.number).append("-").append(cart.phone).append(",");
        }

        Map<String, String> params = new HashMap<>();
        params.put("favItems", favItems.toString());

        Http.Response response = POST(Router.reverse("controllers.ResalerCarts.confirmCarts"), params);
        assertStatus(302, response);
        assertEquals(0, ResalerCart.findAll(resaler).size());
        assertEquals(1, Order.count());
    }

    @Test
    public void 测试批量添加() {
        long itemSize = ResalerCart.count("resaler = ? and goods = ?", resaler, goods);
        ResalerCart singleCart = ResalerCart.find("byResalerAndGoodsAndPhone", resaler,goods, "13472581853").first();
        long phoneSize = singleCart.number;

        Map<String, String> params = new HashMap<>();
        params.put("goodsId",String.valueOf(goods.id));
        params.put("phones", "13472581853 13472581840");

        Http.Response response = POST(Router.reverse("controllers.ResalerCarts.formAdd"), params);
        assertStatus(302, response);

        assertEquals(itemSize + 1, ResalerCart.count("resaler = ? and goods = ?", resaler, goods));
        singleCart.refresh();
        assertEquals(phoneSize + 1, singleCart.number);
    }

    @Test
    public void 测试修改订单数量() {
        ResalerCart singleCart = ResalerCart.find("byResalerAndGoodsAndPhone", resaler,goods, "13472581853").first();
        long phoneSize = singleCart.number;

        Map<String, String> params = new HashMap<>();
        params.put("goodsId",String.valueOf(goods.id));
        params.put("phone", "13472581853");
        params.put("increment", "1");

        Http.Response response = POST(Router.reverse("controllers.ResalerCarts.reorder"), params);
        assertIsOk(response);

        singleCart.refresh();
        assertEquals(phoneSize + 1, singleCart.number);
    }

    @Test
    public void 测试批量删除() {
        assertTrue(ResalerCart.count("resaler = ? and goods = ?", resaler, goods) > 0);

        Map<String, String> params = new HashMap<>();
        params.put("goodsIds",String.valueOf(goods.id));

        Http.Response response = POST(Router.reverse("controllers.ResalerCarts.formBatchDelete"), params);
        assertStatus(302, response);

        assertEquals(0, ResalerCart.count("resaler = ? and goods = ?", resaler, goods));

    }

    @Test
    public void 测试删除单个商品的单个用户() {
        Long count =  ResalerCart.count("resaler = ? and goods = ?", resaler, goods);
        assertTrue(count >= 2);

        Map<String, String> params = new HashMap<>();
        params.put("goodsId",String.valueOf(goods.id));
        params.put("phone","13472581853");

        Http.Response response = POST(Router.reverse("controllers.ResalerCarts.formDelete"), params);
        assertStatus(302, response);

        assertEquals(count-1, ResalerCart.count("resaler = ? and goods = ?", resaler, goods));
    }

    @Test
    public void 测试json删除单个商品的单个用户() {
        Long count =  ResalerCart.count("resaler = ? and goods = ?", resaler, goods);
        assertTrue(count >= 2);


        Map<String, String> params = new HashMap<>();
        params.put("goodsId",String.valueOf(goods.id));
        params.put("phone","13472581853");

        Http.Response response = POST(Router.reverse("controllers.ResalerCarts.formDelete"), params);
        assertStatus(302, response);

        assertEquals(count-1, ResalerCart.count("resaler = ? and goods = ?", resaler, goods));
    }

}
