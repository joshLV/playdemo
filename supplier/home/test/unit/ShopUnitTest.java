package unit;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import models.sales.Shop;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.ModelPaginator;
import play.test.UnitTest;

import java.util.List;

public class ShopUnitTest extends UnitTest {
    Shop shop;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
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
//        condition.name = "shop";
        condition.address = "宛平南路2号";
        condition.supplierId = shop.supplierId;
        ModelPaginator<Shop> shops = Shop.query(condition, 1, 10);
        assertEquals(1, shops.size());
    }

    @Test
    public void testGetAreaName() {
        assertEquals("上海市", shop.getAreaName(2));
        assertEquals("徐汇区", shop.getAreaName(1));
        assertEquals("徐家汇", shop.getAreaName(0));
    }

    @Test
    public void testDelete() {
        Long shopId = shop.id;
        boolean result = Shop.delete(shopId);
        assertTrue(result);

        Shop shop = Shop.findById(shopId);
        assertEquals(DeletedStatus.DELETED, shop.deleted);
    }

    @Test
    public void testGetDistrictIds() {
        String districtId = shop.getDistrictId();
        assertEquals("02102", districtId);
    }

    @Test
    public void testHasMap() {
        assertTrue(shop.hasMap());
    }

    @Test
    public void testHasMap_经度为零() {
        shop.longitude = "0";
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
        shop.latitude = "0.0";
        assertFalse(shop.hasMap());
    }

    @Test
    public void testHasMap_经度为空() {
        shop.longitude = null;
        assertFalse(shop.hasMap());
    }

    @Test
    public void testHasMap_纬度为空() {
        shop.latitude = null;
        assertFalse(shop.hasMap());
    }
}
