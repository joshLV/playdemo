package function;

import com.uhuila.common.constants.DeletedStatus;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.admin.OperateUser;
import models.sales.Brand;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.Play;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

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
    Brand brand;

    /**
     * 测试数据准备
     */
    @Before
    public void setup() {
        // 重新加载配置文件
        FactoryBoy.delete(Brand.class);

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
    }

    @Test
    public void testUpdate() {
        String params = "brand.name=test&brand.description=test-update&brand.logo=abc&brand.displayOrder=0&brand.supplier=Supplier1&brand.logo=" +
                brand.logo + "&brand.deleted=UN_DELETED&brand.introduce=0";

        Http.Response response = PUT("/brands/" + brand.id, "application/x-www-form-urlencoded", params);
        assertStatus(302, response);
        brand.refresh();

        assertEquals("test", brand.name);
    }

    // 测试能否删除品牌信息
    @Test
    public void testDelete() {
        Http.Response response = DELETE("/brands/" + brand.id);
        assertStatus(302, response);

        brand.refresh();

        assertEquals(DeletedStatus.DELETED, brand.deleted);
    }

    /**
     * 测试添加品牌
     */
    // 同样存在 POST 图片文件 在测试环境 接受不了的问题，跳过
    @Test
    @Ignore
    public void testCreate() {
        List<Brand> list = Brand.findAll();
        int count = list.size();
        Map<String, String> brandParams = new HashMap<>();
        brandParams.put("brand.name", "test-brand");
        brandParams.put("brand.description", "test description");
        brandParams.put("brand.logo", "logo.jpg");
        brandParams.put("brand.siteDisplayImage", "test.jpg");
        Http.Response response = POST("/brands", brandParams);
        response.setContentTypeIfNotSet("text/html; charset=GBK");
        assertStatus(200, response);
        list = Brand.findAll();
        assertEquals(count + 1, list.size());
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

}
