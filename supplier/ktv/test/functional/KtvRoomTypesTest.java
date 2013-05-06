package functional;

import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import models.admin.SupplierUser;
import models.ktv.KtvRoomType;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.test.FunctionalTest;

/**
 * User: yan
 * Date: 13-4-11
 * Time: 下午6:16
 */
public class KtvRoomTypesTest extends FunctionalTest {
    /*
    Supplier supplier;
    SupplierUser supplierUser;
    KtvRoomType ktvRoomType;


    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        supplier = FactoryBoy.create(Supplier.class);
        supplierUser = FactoryBoy.create(SupplierUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUser.loginName);

        ktvRoomType = FactoryBoy.create(KtvRoomType.class);
        ktvRoomType.supplier = supplier;
        ktvRoomType.save();
    }

    @Test
    public void testIndex() {
        Http.Response response = GET(Router.reverse("KtvRoomTypes.index").url);
        assertIsOk(response);
        List<KtvRoomType> ktvRoomTypeList = (List) renderArgs("ktvRoomTypeList");
        assertEquals(1, ktvRoomTypeList.size());
    }

    @Test
    public void testAdd() {
        Http.Response response = GET(Router.reverse("KtvRoomTypes.add").url);
        assertIsOk(response);
        assertContentMatch("新增包厢", response);
    }

    @Test
    public void testEdit() {
        Http.Response response = GET(Router.reverse("KtvRoomTypes.edit?id=" + ktvRoomType.id).url);
        assertIsOk(response);
        KtvRoomType ktvRoomType1 = (KtvRoomType) renderArgs("ktvRoomType");
        assertEquals(ktvRoomType1, ktvRoomType);
    }

    @Test
    public void testCreate() {
        assertEquals(1, KtvRoomType.count());
        Map<String, String> params = new HashMap<>();
        params.put("ktvRoomType.name", "小包厢");
        Http.Response response = POST(Router.reverse("KtvRoomTypes.create").url, params);
        assertStatus(302, response);
        assertEquals(2, KtvRoomType.count());
    }

    @Test
    public void testUpdate() {
        Map<String, String> params = new HashMap<>();
        params.put("ktvRoomType.name", "mini room");
        params.put("id", ktvRoomType.id.toString());
        Http.Response response = POST(Router.reverse("KtvRoomTypes.update").url, params);
        assertStatus(302, response);
        ktvRoomType.refresh();
        assertEquals("mini room", ktvRoomType.name);
    }

    @Test
    public void testDelete() {
        Http.Response response = DELETE(Router.reverse("KtvRoomTypes.delete?id=" + ktvRoomType.id).url);
        assertStatus(302, response);
        assertEquals(0, KtvRoomType.count());
    }
    */

}
