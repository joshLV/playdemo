package functional;

import controllers.SuppliersCategory;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.operator.OperateUser;
import models.sales.Sku;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 13-2-28
 * Time: 上午11:56
 */
public class SkuTest extends FunctionalTest {
    Sku sku;

    @Before
    public void setUp() {
        FactoryBoy.delete(Sku.class);
        sku = FactoryBoy.create(Sku.class);
        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/sku");
        assertIsOk(response);
        JPAExtPaginator<Sku> skuList = (JPAExtPaginator) renderArgs("skuList");
        assertEquals(1, skuList.size());
    }

    @Test
    public void testCreate() {
        long count = Sku.count();
        Map<String, String> params = new HashMap<>();
        params.put("sku.name", "test");
        params.put("sku.marketPrice", "10.0");
        params.put("sku.stock", "100");
        params.put("sku.supplierId", sku.supplierId.toString());
        params.put("sku.brand.id", sku.brand.id.toString());
        params.put("sku.supplierCategory.id", sku.supplierCategory.id.toString());

        Http.Response response = POST("/sku", params);
        assertStatus(302, response);
        assertEquals(count + 1, Sku.count());
    }

    @Test
    public void testAdd() {
        Http.Response response = GET("/sku/new");
        assertIsOk(response);
        List<Supplier> supplierList = (List) renderArgs("supplierList");
        List<SuppliersCategory> suppliersCategoryList = (List) renderArgs("supplierCategoryList");
        assertEquals(1, supplierList.size());
        assertEquals(1, suppliersCategoryList.size());

    }

    @Test
    public void testEdit() {
        Http.Response response = GET("/sku/" + sku.id + "/edit");
        assertIsOk(response);
        List<Supplier> supplierList = (List) renderArgs("supplierList");
        List<SuppliersCategory> suppliersCategoryList = (List) renderArgs("supplierCategoryList");
        assertEquals(1, supplierList.size());
        assertEquals(1, suppliersCategoryList.size());
        Sku oldSku = (Sku) renderArgs("sku");
        assertEquals(oldSku.id, sku.id);
    }

    @Test
    public void testUpdate() {
        String params = "sku.stock=100&sku.marketPrice=100&sku.name=edit_sku&sku.supplierId=" + sku.supplierId.toString() + "&sku.brand.id=" + sku.brand.id + "&sku.supplierCategory.id=" + sku.supplierCategory.id;
        Http.Response response = PUT("/sku/" + sku.id, "application/x-www-form-urlencoded", params);
        assertStatus(302, response);

        sku.refresh();
        assertEquals("edit_sku", sku.name);
        assertEquals(100, sku.stock.intValue());
    }

    @Test
    public void testDelete() {
        Http.Response response = DELETE("/sku/" + sku.id);
        assertStatus(302, response);
    }
}
