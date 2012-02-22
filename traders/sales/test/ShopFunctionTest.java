import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import junit.framework.Assert;
import models.sales.Shop;
import play.mvc.Http.Response;
import play.test.FunctionalTest;

/**
 * 门店功能测试
 * @author xuefuwei
 *
 */

public class ShopFunctionTest extends FunctionalTest {

    @Test
    public void create(){
        
        List<Shop> list = Shop.findAll();

        Map<String,String> shop = new HashMap<String,String>();
           
        shop.put("shop.name","xxxxx");
        shop.put("shop.address","bbbbb");
        shop.put("shop.phone","ccccc");
        shop.put("shop.companyId","1");
        
        Response response2 = POST("/shops/create",shop);
        
        
        Assert.assertTrue(response2.status == 302);
        
        List<Shop> list2 = Shop.findAll();

        Assert.assertTrue(list.size() + 1 == list2.size());
        
    }

}
