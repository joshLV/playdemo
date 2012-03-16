package unit;

import com.uhuila.common.constants.DeletedStatus;
import models.sales.*;
import org.junit.Test;
import play.modules.paginate.ModelPaginator;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.List;

public class ShopUnitTest extends UnitTest {
    @org.junit.Before
    public void setup() {
        Fixtures.delete(Shop.class);
        Fixtures.delete(Area.class);
        Fixtures.loadModels("fixture/areas_unit.yml");
        Fixtures.loadModels("fixture/shops_unit.yml");
    }

    @Test
    public void testCreate() {
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

    @Test
    public void testQuery() {
        Shop condition = new Shop();
        condition.name = "宛平南路";
        condition.address = "宛平南路";
        condition.companyId = 1;
        ModelPaginator<Shop> shops = Shop.query(condition, 1, 10);
        assertEquals(1, shops.size());
    }

    @Test
    public void testDelete() {
        Long shopId = (Long) Fixtures.idCache.get("models.sales" +
                ".Shop-Shop_5");
        boolean result = Shop.delete(shopId);
        assertTrue(result);

        Shop shop = Shop.findById(shopId);
        assertEquals(DeletedStatus.DELETED, shop.deleted);
    }
}
