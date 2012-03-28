package function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.sales.Area;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.sales.GoodsStatus;
import models.sales.Shop;
import models.supplier.Supplier;
import navigation.RbacLoader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.Play;
import play.mvc.Http;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import com.uhuila.common.constants.DeletedStatus;
import controllers.supplier.cas.Security;

public class SupplierGoodsTest extends FunctionalTest {

    @Before
    public void setup() {
        Fixtures.delete(Shop.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(Category.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(Area.class);
		Fixtures.delete(SupplierUser.class);
		Fixtures.delete(SupplierRole.class);
		Fixtures.delete(Supplier.class);
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
        
        Long id = (Long) Fixtures.idCache.get("models.admin.SupplierUser-user3");
		SupplierUser user = SupplierUser.findById(id);		
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
        Long goodsId = (Long) Fixtures.idCache.get("models.sales" +
                ".Goods-Goods_001");

        Http.Response response = GET("/goods/" + goodsId + "/view");
        assertIsOk(response);
        assertContentType("text/html", response);
    }

    /**
     * 修改商品信息
     */
    @Test
    public void testEdit() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales" +
                ".Goods-Goods_001");
        System.out.println("goodsId:" + goodsId);
        Http.Response response = GET("/goods/" + goodsId + "/edit");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(Play.defaultWebEncoding, response);

        Map<String, String> goodsParams = new HashMap<>();
        goodsParams.put("goods.name", "test");
        goodsParams.put("id", String.valueOf(goodsId));
        //todo
//        response = PUT("/goods/" + goodsId, goodsParams);
//        assertStatus(302, response);
//        SupplierGoods goods = SupplierGoods.findById(goodsId);
//        Assert.assertEquals(goods.name, "test");
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
        Assert.assertNotNull(list);
    }

    /**
     * 删除商品信息
     */
    @Test
    public void testDelete() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales" +
                ".Goods-Goods_003");
        Map<String, Long[]> goodsParams = new HashMap<>();
        Long[] ids = new Long[]{goodsId};
        goodsParams.put("ids", ids);
        Response response = DELETE("/goods/0?ids[]=" + goodsId);
        assertStatus(302, response);
        Goods goods = Goods.findById(goodsId);
        Assert.assertEquals(DeletedStatus.DELETED, goods.deleted);
    }

    /**
     * 修改商品上下架
     */
    @Test
    public void updateStatus() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales" +
                ".Goods-Goods_004");
        Map<String, String> goodsParams = new HashMap<>();
        goodsParams.put("ids", String.valueOf(goodsId));
        goodsParams.put("status", GoodsStatus.ONSALE.toString());
        Response response = POST("/updatestatus", goodsParams);
        assertStatus(302, response);
        Goods goods = Goods.findById(goodsId);
        Assert.assertEquals(goods.status, GoodsStatus.ONSALE);
    }
}