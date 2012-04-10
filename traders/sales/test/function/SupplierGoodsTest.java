package function;

import com.uhuila.common.constants.DeletedStatus;
import controllers.supplier.cas.Security;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.sales.*;
import models.supplier.Supplier;
import navigation.RbacLoader;
import org.junit.*;
import play.Play;
import play.mvc.Http;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Http.Response response = GET("/goods/" + goodsId);
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


        long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_003");

        Response response = DELETE("/goods/" + goodsId);
        assertStatus(302, response);

        //修改商品状态为下架状态
        response = PUT("/goods/" + goodsId + "/offSale", "text/html", "");
        //再次删除
        response = DELETE("/goods/" + goodsId);
        assertStatus(302, response);

        //验证状态改为已删除状态
        Goods goods1 = Goods.findById(goodsId);
        assertEquals(DeletedStatus.DELETED, goods1.deleted);
    }
}