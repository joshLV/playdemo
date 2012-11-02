package functional;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.cms.CmsQuestion;
import models.sales.Brand;
import models.sales.Goods;
import models.sales.MaterialType;
import models.sales.Shop;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.Play;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * 商品控制器的测试.
 * <p/>
 * User: sujie
 * Date: 2/24/12
 * Time: 5:22 PM
 */
public class GoodsTest extends FunctionalTest {
    Goods goods;
    Brand brand;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        goods = FactoryBoy.create(Goods.class);
        brand = FactoryBoy.create(Brand.class);
        goods.brand = brand;
        goods.save();
    }

    @Test
    public void testShow() {
        Http.Response response = GET("/p/" + goods.id);
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(Play.defaultWebEncoding, response);
        assertContentMatch("Product Name", response);
        Goods goods = (Goods) renderArgs("goods");
        assertEquals(new BigDecimal("10.00"), goods.faceValue);
        assertEquals(new BigDecimal("8.50"), goods.salePrice);
        assertEquals("来一份", goods.brand.name);
        assertEquals("8.5折", goods.getDiscountExpress());
        assertEquals(Long.valueOf("10"), goods.getVirtualSaleCount());
        Collection<Shop> shops = goods.getShopList();
        assertEquals(1, shops.size());
        assertEquals("测试店", shops.iterator().next().name);
        assertEquals("宛平南路2号", shops.iterator().next().address);
        assertNotNull(goods.getDetails());
        assertNotNull(goods.getPrompt());
        assertNotNull(goods.getExhibition());
    }

    @Test
    public void testShops() {
        Http.Response response = GET("/goods/" + goods.id + "/shops?currPage=1&pageSize=5");
        assertIsOk(response);
        assertContentType("application/json", response);
        assertStatus(200, response);
        Collection<Shop> shops = (Collection) renderArgs("shops");
        int pageNumber = (Integer) renderArgs("pageNumber");
        int pageSize = (Integer) renderArgs("pageSize");
        assertEquals(1, shops.size());
        assertEquals(1, pageNumber);
        assertEquals(5, pageSize);
        assertEquals("测试店", shops.iterator().next().name);
        assertEquals("宛平南路2号", shops.iterator().next().address);
    }

    @Test
    public void testQuestions() {
        CmsQuestion cmsQuestion = FactoryBoy.create(CmsQuestion.class);
        cmsQuestion.goodsId = goods.id;
        cmsQuestion.save();
        Http.Response response = GET("/goods/" + goods.id + "/questions?currPage=1&pageSize=5");
        assertIsOk(response);
        assertContentType("application/json", response);
        assertStatus(200, response);
        List<CmsQuestion> cmsQuestions = (List) renderArgs("cmsQuestions");
        int pageNumber = (Integer) renderArgs("pageNumber");
        int pageSize = (Integer) renderArgs("pageSize");
        assertEquals(1, cmsQuestions.size());
        assertEquals(1, pageNumber);
        assertEquals(5, pageSize);
        assertEquals("满百送电影票活动，是不是拍一张这个面值一百的就可以了？还是这个只算80块？", cmsQuestions.get(0).content);
        assertEquals("亲，满百送电影票需要购物金额到100元哦~如果只买1张100的券算80哟！", cmsQuestions.get(0).reply);
    }

    @Ignore
    @Test
    public void testList() {
        FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.materialType = MaterialType.ELECTRONIC;
            }
        });
        Http.Response response = GET("/s/0-021-0-0-0-0-0-0-1?page=1");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(Play.defaultWebEncoding, response);
    }
}
