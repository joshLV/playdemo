package functional;

import com.uhuila.common.constants.DeletedStatus;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.operator.OperateUser;
import models.sales.Area;
import models.sales.Shop;
import models.supplier.Supplier;
import org.junit.Test;
import play.modules.paginate.ModelPaginator;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运营后台的门店管理功能测试.
 * <p/>
 * User: sujie
 * Date: 1/7/13
 * Time: 11:03 AM
 */
public class OperateShopsTest extends FunctionalTest {
    @org.junit.Before
    public void setup() {
        FactoryBoy.deleteAll();

        FactoryBoy.batchCreate(20, Shop.class, new SequenceCallback<Shop>() {
            @Override
            public void sequence(Shop target, int seq) {
                target.name = "name" + seq;
            }
        });

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/shops");
        assertIsOk(response);

        ModelPaginator<Shop> shopPage = (ModelPaginator<Shop>) renderArgs("shopPage");
        assertEquals(20, shopPage.size());
    }

    @Test
    public void testAdd() {
        Http.Response response = GET("/shops/new");
        assertIsOk(response);

        List<Area> districts = (List<Area>) renderArgs("districts");
        assertEquals(1, districts.size());
        List<Area> areas = (List<Area>) renderArgs("areas");
        assertEquals(1, areas.size());
        List<Area> cities = (List<Area>) renderArgs("cities");
        assertEquals(1, cities.size());
        List<Supplier> supplierList = (List<Supplier>) renderArgs("supplierList");
        assertEquals(1, supplierList.size());
    }

    @Test
    public void testCreate() {
        long count = Shop.count();
        Map<String, String> params = new HashMap<>();
        params.put("shop.name", "test-shop");
        params.put("shop.address", "test-address");
        params.put("shop.phone", "test-phone");
        params.put("shop.latitude", "1.2");
        params.put("shop.longitude", "2.2");
        params.put("shop.areaId", FactoryBoy.last(Shop.class).areaId);
        params.put("shop.supplierId", String.valueOf(FactoryBoy.last(Shop.class).supplierId));

        Http.Response response = POST("/shops", params);
        assertStatus(302, response);

        assertEquals(count + 1, Shop.count());
    }

    @Test
    public void testDelete() {
        Shop shop = FactoryBoy.last(Shop.class);
        assertEquals(DeletedStatus.UN_DELETED, shop.deleted);

        Http.Response response = DELETE("/shops/" + shop.id);
        assertStatus(302, response);

        shop.refresh();
        assertEquals(DeletedStatus.DELETED, shop.deleted);
    }


    @Test
    public void testEdit() {
        final Shop shop = FactoryBoy.last(Shop.class);

        Http.Response response = GET("/shops/" + shop.id + "/edit");
        assertIsOk(response);

        Shop originalShop = (Shop) renderArgs("shop");
        assertNotNull(originalShop);
        assertEquals(shop.id, originalShop.id);
        List<Area> districts = (List<Area>) renderArgs("districts");
        assertEquals(1, districts.size());
        List<Area> areas = (List<Area>) renderArgs("areas");
        assertEquals(1, areas.size());
        List<Area> cities = (List<Area>) renderArgs("cities");
        assertEquals(1, cities.size());
        List<Supplier> supplierList = (List<Supplier>) renderArgs("supplierList");
        assertEquals(1, supplierList.size());
    }

    @Test
    public void testUpdate() {
        Shop shop = FactoryBoy.last(Shop.class);

        String params = "shop.name=updated_shop&shop.address=updated_address";
        Http.Response response = PUT("/shops/" + shop.id, "application/x-www-form-urlencoded", params);
        assertStatus(302, response);

        shop.refresh();
        assertEquals("updated_shop", shop.name);
        assertEquals("updated_address", shop.address);
    }

    @Test
    public void testShowGoodsShops() {
        Http.Response response = GET("/shops/" + FactoryBoy.last(Supplier.class).id + "/showGoodsShops");
        assertIsOk(response);

        List<Shop> shopList = (List<Shop>) renderArgs("shopList");
        assertEquals(20, shopList.size());
    }


    @Test
    public void testShowSupplierShops() {
        Http.Response response = GET("/shops/" + FactoryBoy.last(Supplier.class).id + "/showSupplierShops");
        assertIsOk(response);

        List<Shop> shopList = (List<Shop>) renderArgs("shopList");
        assertEquals(20, shopList.size());
    }

}
    