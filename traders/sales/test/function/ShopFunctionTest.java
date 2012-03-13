package function;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import junit.framework.Assert;
import models.sales.Goods;
import models.sales.GoodsStatus;
import models.sales.Shop;
import play.Play;
import play.mvc.Http;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

/**
 * 门店功能测试
 * @author xuefuwei
 *
 */
@Ignore
@Ignore
public class ShopFunctionTest extends FunctionalTest {


	@org.junit.Before
	public void setup() {
		Fixtures.delete(Goods.class);
		Fixtures.loadModels("fixture/shops.yml");
	}
	
    @Test
    public void create(){
        
        List<Shop> list = Shop.findAll();

        Map<String,String> shop = new HashMap<String,String>();
           
        shop.put("shop.name","xxxxx");
        shop.put("shop.address","bbbbb");
        shop.put("shop.phone","ccccc");
        shop.put("shop.companyId","1");
        
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
		Assert.assertEquals(shop.name,"test");  
	}

}
