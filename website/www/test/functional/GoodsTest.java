package functional;

import models.sales.Goods;
import models.sales.MaterialType;

import org.junit.Before;
import org.junit.Test;

import play.Play;
import play.mvc.Http;
import play.test.FunctionalTest;
import factory.FactoryBoy;
import factory.callback.BuildCallback;

/**
 * 商品控制器的测试.
 * <p/>
 * User: sujie
 * Date: 2/24/12
 * Time: 5:22 PM
 */
public class GoodsTest extends FunctionalTest {
    Goods goods;
	@Before
    public void setUp() {
    	FactoryBoy.deleteAll();
    	goods = FactoryBoy.create(Goods.class);
    }

    @Test
    public void testShow() {
        Http.Response response = GET("/g/" + goods.id);
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(Play.defaultWebEncoding, response);
    }	


    @Test
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

}
