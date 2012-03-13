package unit;
import java.util.List;

import models.sales.Goods;
import models.sales.Shop;
import org.junit.Assert;
import org.junit.Test;

import com.uhuila.common.constants.DeletedStatus;

import play.test.UnitTest;

public class ShopsTest extends UnitTest {
    @Test
    public void testCreate(){
        Shop shop = new Shop();
        shop.companyId = 1l;
        shop.areaId = "02101001";
        shop.name = "北京";
        shop.address = "上海";
        shop.deleted = DeletedStatus.UN_DELETED;
        shop.save();
		List<Shop> list = Shop.findAll();
		assertNotNull(list);
    }
}
