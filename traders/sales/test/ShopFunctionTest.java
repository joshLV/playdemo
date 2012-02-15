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
        
        //List<Shop> list = Shop.findAll();
        
        Map<String,String> shop = new HashMap<String,String>();
        
       // Shop sh = new Shop();
//        sh.name = "用户名称";
//        sh.address = "用户地址";
//        sh.phone = "用户手机";
//        sh.company_id = 1;
//        shop.put("shop", sh);
//        
        shop.put("shop.name","用户名称");
        shop.put("shop.address","用户名称");
        shop.put("shop.phone","用户名称");
        shop.put("shop.company_id","1");
        
        
        System.out.println("ddddddd");
        
        Response response2 = POST("shops/create",shop);
        
//        
//        System.out.println(response2.status);
//        Assert.assertTrue(response2.status == 302);
//        
//        List<Shop> list2 = Shop.findAll();
//        System.out.println(list2.size());
//        
//        Assert.assertTrue(list.size() + 1 == list2.size());
        
    }

}
