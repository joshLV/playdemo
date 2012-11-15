package function;

import controllers.modules.resale.cas.Security;
import factory.FactoryBoy;
import models.resale.ResalerFav;
import models.sales.Goods;
import models.sales.GoodsDeployRelation;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

/**
 * <p/>
 * User: yanjy
 * Date: 12-11-15
 * Time: 下午4:29
 */
public class DDPushGoodsTest extends FunctionalTest {
    Goods goods;
    ResalerFav resalerFav;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        resalerFav = FactoryBoy.create(ResalerFav.class);
        resalerFav.resaler.loginName = "dangdang";
        resalerFav.save();
        goods = resalerFav.goods;
        Security.setLoginUserForTest(resalerFav.resaler.loginName);

    }

    @Test
    public void 测试向当当推送商品_异常情况() {
        goods.effectiveAt = null;
        goods.save();
        Http.Response response = GET("/dangdang-batch-add?goodsIds=" + goods.id);
        assertStatus(200, response);
        assertEquals("{\"error\":\"true\",\"info\":\"商品ID=" + goods.id + ",\"}", response.out.toString());
    }

    @Test
    public void 测试向当当推送商品_正常情况() {
        Long n = GoodsDeployRelation.count();
        Http.Response response = GET("/dangdang-batch-add?goodsIds=" + goods.id);
        assertStatus(200, response);
        assertEquals("{\"error\":\"false\",\"info\":\"\"}", response.out.toString());
        assertEquals(n + 1, GoodsDeployRelation.count());
    }

}
