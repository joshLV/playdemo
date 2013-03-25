package function;

import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import models.admin.SupplierUser;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;

/**
 * User: tanglq
 * Date: 13-3-25
 * Time: 下午7:17
 */
public class SupplierWeixinBindsTest extends FunctionalTest {
    SupplierUser supplierUser;

    @Before
    public void setup() {
        FactoryBoy.lazyDelete();

        supplierUser = FactoryBoy.create(SupplierUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUser.loginName);
    }


    /**
     * 访问微信内容页面，会看到一个识别码.
     *
     * @throws Exception
     */
    @Test
    public void testShowNoBindIndex() throws Exception {
        Http.Response response = GET(Router.reverse("SupplierWeixinBinds.index").url);
        assertIsOk(response);
        supplierUser.refresh();
        assertNotNull(supplierUser.idCode);
        assertContentMatch("向『一百券商家助手』发出您的身份识别码", response);
    }

    /**
     * 已经绑定的用户查看信息会不同.
     * @throws Exception
     */
    @Test
    public void testShowBindedIndex() throws Exception {
        supplierUser.weixinOpenId = "3232412341234124";
        supplierUser.idCode = null;
        supplierUser.save();
        Http.Response response = GET(Router.reverse("SupplierWeixinBinds.index").url);
        assertIsOk(response);
        supplierUser.refresh();
        assertNotNull(supplierUser.weixinOpenId);
        assertContentMatch("解绑微信", response);
    }

    /**
     * 解绑.
     * @throws Exception
     */
    @Test
    public void testUnbindWeixi() throws Exception {
        supplierUser.weixinOpenId = "3232412341234124";
        supplierUser.save();
        Http.Response response = POST(Router.reverse("SupplierWeixinBinds.unbind").url);
        assertStatus(302, response);
        supplierUser.refresh();
        assertNull(supplierUser.weixinOpenId);
    }
}
