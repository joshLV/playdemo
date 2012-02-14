import org.junit.Assert;
import org.junit.Test;

import play.test.UnitTest;
import models.sales.Shops;
import models.sales.Shop;

public class ShopsTest extends UnitTest {

    @Test
    public void deleted(){
        
        Shops shops = new Shops();
        
        Shop shop = new Shop();
        shop.company_id = 1;
        shop.area_id = 0;
        shop.name  = "北京";
        shop.address = "上海";
        shop.deleted = 0;        
        shop.save();
        
        shops.deleted(shop.id);
        
        Shop sh = Shop.findById(shop.id);
        Assert.assertNotNull(sh);
        Assert.assertEquals(sh.deleted,1);

    }
    
    @Test
    public void delete(){
        Shops shops = new Shops();
        
        Shop shop = new Shop();
        shop.company_id = 1;
        shop.area_id = 0;
        shop.name  = "北京";
        shop.address = "上海";
        shop.deleted = 0;        
        shop.save();
        
        shops.delete(shop.id);
        
        Shop sh = Shop.findById(shop.id);
        
        Assert.assertTrue(sh == null);
        
        
    }
    
    
}
