package functional;

import models.sales.Goods;
import models.sales.MaterialType;

import models.sales.Area;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.sales.Shop;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import play.Play;
import play.mvc.Http;
import play.test.FunctionalTest;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.callback.SequenceCallback;

import java.util.List;

/**
 * 商品控制器的测试.
 * <p/>
 * User: sujie
 * Date: 2/24/12
 * Time: 5:22 PM
 */
@Ignore
public class GoodsTest extends FunctionalTest {
    Goods goods;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        goods = FactoryBoy.create(Goods.class);
    }

    //    @Test
    public void testShow() {
        Http.Response response = GET("/g/" + goods.id);
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(Play.defaultWebEncoding, response);
    }


    //    @Test
    public void testList() {
    	FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
			@Override
			public void build(Goods g) {
				g.materialType = MaterialType.REAL;
			}
		});
        Http.Response response = GET("/s/0-021-0-0-0-0-0-0-1?page=1");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(Play.defaultWebEncoding, response);
    }

    @Test
    public void testSearch() {
        List<Goods> goodsList = Goods.search("name", "抵用券").fetch();
        assertNotNull(goodsList);
        assertEquals(2, goodsList.size());
    }


}
