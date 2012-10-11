package unit;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import models.sales.Area;
import models.sales.Shop;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.ModelPaginator;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.List;

public class ShopUnitTest extends UnitTest {
    Shop shop;
    @Before
    public void setup() {
        Fixtures.delete(Shop.class);
        Fixtures.delete(Area.class);
        Fixtures.loadModels("fixture/areas_unit.yml");
        Fixtures.loadModels("fixture/shops_unit.yml");

        FactoryBoy.lazyDelete();
        shop = FactoryBoy.create(Shop.class);

    }

    @Test
    public void testCreate() {
        Shop shop = new Shop();
        shop.supplierId = 1l;
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
        condition.supplierId = 1;
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


    @Test
    public void testHasMap() {
        assertTrue(shop.hasMap());
    }

    @Test
    public void testHasMap_经度为零() {
        shop.longitude="0";
        assertFalse(shop.hasMap());
    }

    @Test
    public void testHasMap_纬度为零() {
        shop.latitude = "0";
        assertFalse(shop.hasMap());
    }

    @Test
    public void testHasMap_经度为00() {
        shop.longitude = "0.0";
        assertFalse(shop.hasMap());
    }

    @Test
    public void testHasMap_纬度为00() {
        shop.longitude = "0.0";
        assertFalse(shop.hasMap());
    }

    @Test
    public void testHasMap_经度为空() {
        shop.longitude = null;
        assertFalse(shop.hasMap());
    }

    @Test
    public void testHasMap_纬度为空() {
        shop.longitude = null;
        assertFalse(shop.hasMap());
    }
}
