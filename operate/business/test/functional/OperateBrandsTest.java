package functional;

import com.uhuila.common.constants.DeletedStatus;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.admin.OperateUser;
import models.sales.Brand;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Play;
import play.data.validation.Error;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 品牌测试.
 * User: Juno
 * Date: 12-7-20
 * Time: 下午3:53
 */

public class OperateBrandsTest extends FunctionalTest {
    Supplier supplier;
    Brand brand;

    @BeforeClass
    public static void setUpClass() {
        Play.tmpDir = new File("/tmp");
    }

    @AfterClass
    public static void tearDownClass() {
        Play.tmpDir = null;
    }

    /**
     * 测试数据准备
     */
    @Before
    public void setup() {
        // 重新加载配置文件
        FactoryBoy.deleteAll();
        supplier = FactoryBoy.create(Supplier.class);
        brand = FactoryBoy.create(Brand.class);

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    // 测试能否进入品牌页面
    @Test
    public void testBrandsDisplay() {
        Http.Response response = GET("/brands");
        assertIsOk(response);
        assertContentType("text/html", response);
    }

    // 测试能否修改商品信息
    @Test
    public void testEdit() {
        Http.Response response = GET("/brands/" + brand.id + "/edit");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertContentMatch("修改品牌", response);
        assertCharset(Play.defaultWebEncoding, response);
        assertEquals(brand, (Brand) renderArgs("brand"));
    }

    @Test
    public void testUpdateInvalid() {
        String params = "brand.description=test-update&brand.logo=abc" +
                "&brand.supplier=Supplier1&brand.deleted=UN_DELETED&brand.introduce=0&brand.logo=" + brand.logo;
        Map<String, Object> urlMap = new HashMap<>();
        urlMap.put("id", brand.id);
        Http.Response response = PUT(Router.reverse("OperateBrands.update", urlMap).url, "application/x-www-form-urlencoded", params);
        assertStatus(200, response);
        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("brand.name", errors.get(0).getKey());
    }

    @Test
    public void testUpdate() {
        VirtualFile vfImage = VirtualFile.fromRelativePath("test/leaves.jpg");
        Map<String, File> fileParams = new HashMap<>();
        fileParams.put("siteDisplayImage", vfImage.getRealFile());
        fileParams.put("logoImage", vfImage.getRealFile());
        Map<String, String> params = new HashMap<>();
        params.put("x-http-method-override", "PUT");
        params.put("brand.name", "test-brand");
        params.put("brand.description", "test-update");
        params.put("brand.logo", "abc");
        params.put("brand.displayOrder", "0");
        params.put("brand.supplier", "Supplier1");
        params.put("brand.logo", brand.logo);
        params.put("brand.deleted", "UN_DELETED");
        params.put("brand.introduce=", "0");

        Map<String, Object> urlMap = new HashMap<>();
        urlMap.put("id", brand.id);
        Http.Response response = POST("/brands/" + brand.id + "?x-http-method-override=PUT", params, fileParams);
        assertStatus(302, response);

        brand.refresh();
        assertEquals("test-brand", brand.name);
    }

    // 测试能否删除品牌信息
    @Test
    public void testDelete() {
        Http.Response response = DELETE("/brands/" + brand.id);
        assertStatus(302, response);
        brand.refresh();
        assertEquals(DeletedStatus.DELETED, brand.deleted);
    }

    // 测试添加品牌的页面显示
    @Test
    public void testAdd() {
        Http.Response response = GET("/brands/new");
        assertStatus(200, response);
        assertEquals(1, ((List<Supplier>) renderArgs("supplierList")).size());
    }

    @Test
    public void testGoodsBrands() {
        Http.Response response = GET("/goods_brands/" + supplier.id);
        assertStatus(200, response);
        assertEquals(1, ((List<Brand>) renderArgs("brandList")).size());
    }

    /**
     * 测试添加品牌
     */
    @Test
    public void testCreate() {
        VirtualFile vfImage = VirtualFile.fromRelativePath("test/leaves.jpg");
        Map<String, File> fileParams = new HashMap<>();
        fileParams.put("siteDisplayImage", vfImage.getRealFile());
        fileParams.put("logoImage", vfImage.getRealFile());

        List<Brand> list = Brand.findAll();
        int count = list.size();
        Map<String, String> brandParams = new HashMap<>();
        brandParams.put("brand.name", "test-brand");
        brandParams.put("brand.description", "test description");
        brandParams.put("brand.logo", "logo.jpg");
        brandParams.put("brand.siteDisplayImage", "test.jpg");
        Http.Response response = POST("/brands", brandParams, fileParams);
        assertStatus(302, response);
        list = Brand.findAll();
        assertEquals(count + 1, list.size());
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

}
