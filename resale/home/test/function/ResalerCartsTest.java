package function;

import controllers.modules.resale.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.resale.Resaler;
import models.resale.ResalerCart;
import models.resale.ResalerFav;
import models.sales.Goods;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;

import java.util.List;

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
}
