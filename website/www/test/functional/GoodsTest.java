package functional;

import models.sales.Goods;
import org.junit.Test;
import play.Play;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;

/**
 * 商品控制器的测试.
 * <p/>
 * User: sujie
 * Date: 2/24/12
 * Time: 5:22 PM
 */
public class GoodsTest extends FunctionalTest {
    @org.junit.Before
    public void setup() {
        Fixtures.delete(Goods.class);
        Fixtures.loadModels("fixture/goods.yml");
    }

    @Test
    public void testShow() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales" +
                ".Goods-goods1");

        Http.Response response = GET("/goods/" + goodsId);
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(Play.defaultWebEncoding, response);
    }

}
