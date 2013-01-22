package function;

import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import models.admin.SupplierUser;
import models.sales.Area;
import models.sales.Shop;
import models.supplier.Supplier;
import navigation.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.data.validation.Error;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.uhuila.common.constants.*;

/**
 * 商户的门店管理功能测试.
 * <p/>
 * User: sujie
 * Date: 12/28/12
 * Time: 9:53 AM
 */
public class SupplierShopsTest extends FunctionalTest {
    Shop shop;
    SupplierUser supplierUser;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        shop = FactoryBoy.create(Shop.class);
        Supplier supplier = Supplier.findById(shop.supplierId);
        supplierUser = FactoryBoy.create(SupplierUser.class);
        supplierUser.supplier = supplier;
        supplierUser.save();

        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUser.loginName);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testIndex() {
        Http.Response response = GET(Router.reverse("SupplierShops.index").url);
        assertIsOk(response);
        assertEquals(1l, ((List<Shop>) renderArgs("shopList")).size());
    }

    @Test
    public void testCreateInvalid() {
        Map<String, String> params = new HashMap<>();
        params.put("shop.address", "bbbbb");
        params.put("shop.phone", "13212345678");
        Http.Response response = POST("/shops", params);
        List<Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("shop.name", errors.get(0).getKey());
        assertStatus(200, response);
    }

    @Test
    public void testCreate() {
        List<Shop> list = Shop.findAll();
        int cnt = list.size();
        assertEquals(1, cnt);
        Map<String, String> params = new HashMap<>();

        params.put("shop.name", "xxxxx");
        params.put("shop.address", "bbbbb");
        params.put("shop.phone", "13212345678");
        Http.Response response = POST("/shops", params);
        assertStatus(302, response);
        list = Shop.findAll();
        assertEquals(cnt + 1, list.size());
    }

    @Test
    public void testAdd() {
        Http.Response response = GET(Router.reverse("SupplierShops.add").url + "?shop.name=" + shop.name
                + "&shop.address=" + shop.address + "&shop.phone=" + shop.phone + "&shop.id=" + shop.id);
        assertIsOk(response);
        assertEquals(shop, (Shop) renderArgs("shop"));
    }

    @Test
    public void testUpdateInvalid() {
        Map<String, String> params = new HashMap<>();
        params.put("shop.add`ress", "bbbbb");
        params.put("shop.phone", "13212345678");
        Http.Response response = POST("/shops/" + shop.id, params);
        List<Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("shop.name", errors.get(0).getKey());
        assertStatus(200, response);
    }


    /**
     * 编辑门店
     */
    @Test
    public void testEdit() {
        Http.Response response = GET("/shops/" + shop.id + "/edit");
        assertIsOk(response);
        Shop shop = (Shop) renderArgs("shop");
        assertEquals("测试店", shop.name);
        List<Area> cities = (List) renderArgs("cities");
        List<Area> districts = (List) renderArgs("districts");
        List<Area> areas = (List) renderArgs("areas");
        assertEquals(1, cities.size());
        assertEquals(1, districts.size());
        assertEquals(1, areas.size());
    }

    /**
     * 测试 update()
     */
    @Test
    public void testUpdate() {
        Map<String, String> goodsParams = new HashMap<>();
        goodsParams.put("shop.name", "test1");
        goodsParams.put("shop.areaId", "021");
        goodsParams.put("shop.address", "wanpingnanlu");
        goodsParams.put("shop.phone", "12345678");
        Http.Response response = POST("/shops/" + shop.id, goodsParams);
        assertStatus(302, response);
        shop.refresh();
        assertEquals("test1", shop.name);
        assertEquals("12345678", shop.phone);
        assertEquals("021", shop.areaId);
        assertEquals("wanpingnanlu", shop.address);
    }

    @Test
    public void testDelete() {
        assertEquals(DeletedStatus.UN_DELETED, shop.deleted);
        Http.Response response = DELETE("/shops/" + shop.id);
        assertStatus(302, response);
        shop.refresh();
        assertEquals(DeletedStatus.DELETED, shop.deleted);
    }

    @Test
    public void testShowAreas() {
        Http.Response response = GET(Router.reverse("SupplierShops.showAreas").url + "?areaId=" + shop.areaId);
        assertIsOk(response);
        assertContentType("application/json", response);
    }

}
    