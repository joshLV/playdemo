package function;

import com.uhuila.common.constants.DeletedStatus;
import controllers.operate.cas.Security;
import models.admin.OperateRole;
import models.admin.OperateUser;
import models.sales.*;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Test;
import play.Play;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperateGoodsTest extends FunctionalTest {

    @org.junit.Before
    public void setup() {
        Fixtures.delete(Shop.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(Category.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(Area.class);

        Fixtures.delete(OperateUser.class);
        Fixtures.delete(OperateRole.class);
        Fixtures.loadModels("fixture/roles.yml");
        Fixtures.loadModels("fixture/supplierusers.yml");

        Fixtures.loadModels("fixture/areas_unit.yml");
        Fixtures.loadModels("fixture/categories_unit.yml");
        Fixtures.loadModels("fixture/brands_unit.yml");
        Fixtures.loadModels("fixture/shops_unit.yml");
        Fixtures.loadModels("fixture/goods_unit.yml");

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        Long id = (Long) Fixtures.idCache.get("models.admin.OperateUser-user3");
        OperateUser user = OperateUser.findById(id);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    /**
     * 查看商品信息
     */
    @Test
    public void testDetails() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");

        Response response = GET("/goods/" + goodsId);
        assertIsOk(response);
        assertContentType("text/html", response);
    }

    /**
     * 添加商品信息
     */
    @Test
    public void testCreate() {
        Map<String, String> goodsParams = new HashMap<>();
        goodsParams.put("goods.name", "laiyifen1");
        goodsParams.put("goods.no", "20000000");
        goodsParams.put("goods.supplierId", "0");
        goodsParams.put("goods.status", GoodsStatus.ONSALE.toString());
        goodsParams.put("goods.prompt", "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
        goodsParams.put("goods.details", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
//        goodsParams.put("goods.imagePath", "/opt/3.jpg");
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
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_003");

        Response response = DELETE("/goods/" + goodsId);
        assertStatus(302, response);
        Goods goods = Goods.findById(goodsId);
        assertEquals(DeletedStatus.DELETED, goods.deleted);
    }

    /**
     * 修改商品上下架
     */
    @Test
    public void testOnSale() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_004");

        Response response = PUT("/goods/" + goodsId + "/onSale", "text/html", "");
        assertStatus(302, response);
        Goods goods = Goods.findById(goodsId);
        assertEquals(GoodsStatus.ONSALE, goods.status);
    }

    /**
     * 修改商品信息
     */
    @Test
    public void testEdit() {
        Long brandId = (Long) Fixtures.idCache.get("models.sales.Brand-Brand_1");
        Long categoryId = (Long) Fixtures.idCache.get("models.sales.Category-Category_1");
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        Response response = GET("/goods/" + goodsId + "/edit");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(Play.defaultWebEncoding, response);

        String params = "goods.name=test123&goods.faceValue=120&goods" +
                ".originalPrice=120&goods.details=abcdefgh&goods.salePrice=123&goods.categories.id=" +
                categoryId + "&goods.expireAt=2015-12-12&goods.effectiveAt=2012-03-12&goods.baseSale=1000" +
                "&levelPrices=1&goods.brand" +
                ".id=" + brandId;
        response = PUT("/goods/" + goodsId, "application/x-www-form-urlencoded", params);
        assertStatus(302, response);
        Goods goods = Goods.findById(goodsId);
        assertEquals("test123", goods.name);
    }
}