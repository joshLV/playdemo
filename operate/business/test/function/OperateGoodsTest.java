package function;

import com.uhuila.common.constants.DeletedStatus;
import models.sales.*;
import org.junit.Ignore;
import org.junit.Test;
import play.Play;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Ignore
public class OperateGoodsTest extends FunctionalTest {

    @org.junit.Before
    public void setup() {
        Fixtures.delete(Shop.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(Category.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(Area.class);
        Fixtures.loadModels("fixture/areas_unit.yml");
        Fixtures.loadModels("fixture/categories_unit.yml");
        Fixtures.loadModels("fixture/brands_unit.yml");
        Fixtures.loadModels("fixture/shops_unit.yml");
        Fixtures.loadModels("fixture/goods_unit.yml");
        
		// 设置测试登录的用户名
//        Security.setLoginUserForTest("test1");
    }
    
    /**
     * 查看商品信息
     */
    @Test
    public void testDetails() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales" +
                ".OperateGoods-Goods_001");

        Response response = GET("/goods/" + goodsId + "/view");
        assertIsOk(response);
        assertContentType("text/html", response);
    }

    /**
     * 修改商品信息
     */
    @Test
    public void testEdit() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales" +
                ".OperateGoods-Goods_001");
        Response response = GET("/goods/" + goodsId + "/edit");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(Play.defaultWebEncoding, response);

        Map<String, String> goodsParams = new HashMap<>();
        goodsParams.put("goods.name", "test");
        goodsParams.put("id", String.valueOf(goodsId));
        response = POST("/goods/" + goodsId + "/update", goodsParams);
        assertStatus(302, response);
        Goods goods = Goods.findById(goodsId);
        assertEquals(goods.name, "test");
    }

    /**
     * 添加商品信息
     */
    @Test
    @Ignore
    public void testCreate() {
        Map<String, String> goodsParams = new HashMap<>();
        goodsParams.put("goods.name", "laiyifen1");
        goodsParams.put("goods.no", "20000000");
        goodsParams.put("goods.supplierId", "0");
        goodsParams.put("goods.status", GoodsStatus.ONSALE.toString());
        goodsParams.put("goods.prompt", "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
        goodsParams.put("goods.details", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        goodsParams.put("goods.imagePath", "/opt/3.jpg");
        goodsParams.put("goods.deleted", DeletedStatus.DELETED.toString());
        goodsParams.put("goods.createdBy", "yanjy");
        Response response = POST("/goods", goodsParams);
        response.setContentTypeIfNotSet("text/html; charset=GBK");
        assertStatus(200, response);
        List<Goods> list = Goods.findAll();
        assertNotNull(list);
    }

    /**
     * 删除商品信息
     */
    @Test
    public void testDelete() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales" +
                ".OperateGoods-Goods_003");
        Map<String, Long[]> goodsParams = new HashMap<>();
        Long[] ids = new Long[]{goodsId};
        goodsParams.put("ids", ids);
        Response response = DELETE("/goods/0/delete?ids[]=" + goodsId);
        assertStatus(302, response);
        Goods goods = Goods.findById(goodsId);
        assertEquals(DeletedStatus.DELETED, goods.deleted);
    }

    /**
     * 修改商品上下架
     */
    @Test
    public void updateStatus() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales" +
                ".OperateGoods-Goods_004");
        Map<String, String> goodsParams = new HashMap<>();
        goodsParams.put("ids", String.valueOf(goodsId));
        goodsParams.put("status", GoodsStatus.ONSALE.toString());
        Response response = POST("/updatestatus", goodsParams);
        assertStatus(302, response);
        Goods goods = Goods.findById(goodsId);
        assertEquals(goods.status, GoodsStatus.ONSALE);
    }
}