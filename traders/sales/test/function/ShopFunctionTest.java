package function;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.Assert;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;
import navigation.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import controllers.supplier.cas.Security;

/**
 * 门店功能测试
 * @author xuefuwei
 *
 */
public class ShopFunctionTest extends FunctionalTest {

	@Before
	public void setup() {
		Fixtures.delete(Goods.class);

		Fixtures.delete(SupplierUser.class);
		Fixtures.delete(SupplierRole.class);
		Fixtures.delete(Supplier.class);
		Fixtures.loadModels("fixture/roles.yml");
		Fixtures.loadModels("fixture/supplierusers.yml");
		
		Fixtures.loadModels("fixture/shops.yml");
		
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
	
    @Test
    public void create(){
        
        List<Shop> list = Shop.findAll();

        Map<String,String> shop = new HashMap<String,String>();
           
        shop.put("shop.name","xxxxx");
        shop.put("shop.address","bbbbb");
        shop.put("shop.phone","ccccc");
        shop.put("shop.supplierId","1");
        
        Response response2 = POST("/shops",shop);
        
        Assert.assertTrue(response2.status == 302);
        List<Shop> list2 = Shop.findAll();
        Assert.assertTrue(list.size() + 1 == list2.size());
    }
    

	/**
	 * 编辑门店
	 */
	@Test
	public void testDetails() {
		Long shopId = (Long) Fixtures.idCache.get("models.sales" +
				".Shop-shop2");
		Map<String, String> goodsParams = new HashMap<String,String>();
		goodsParams.put("shop.name", "test");
		Response response = POST("/shops/"+shopId, goodsParams);
		assertStatus(302,response);
		Shop shop = Shop.findById(shopId);
		Assert.assertEquals("一百券二店", shop.name);
	}

}
