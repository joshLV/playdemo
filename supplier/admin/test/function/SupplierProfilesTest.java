package function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import factory.callback.BuildCallback;
import models.admin.SupplierRole;
import models.admin.SupplierUser;

import org.junit.Before;
import org.junit.Test;

import play.data.validation.*;
import play.data.validation.Error;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import controllers.supplier.cas.Security;
import factory.FactoryBoy;

public class SupplierProfilesTest extends FunctionalTest {
    SupplierUser supplierUser;
    SupplierRole role;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        role = FactoryBoy.create(SupplierRole.class);
        SupplierRole roleClerk = FactoryBoy.create(SupplierRole.class, new BuildCallback<SupplierRole>() {
            @Override
            public void build(SupplierRole role) {
                role.text = "收银人员";
                role.key = "clerk";
            }
        });


        SupplierRole roleAdmin = FactoryBoy.create(SupplierRole.class, new BuildCallback<SupplierRole>() {
            @Override
            public void build(SupplierRole role) {
                role.text = "系统管理员";
                role.key = "admin";
            }
        });

        SupplierRole roleSales = FactoryBoy.create(SupplierRole.class, new BuildCallback<SupplierRole>() {
            @Override
            public void build(SupplierRole role) {
                role.text = "销售人员";
                role.key = "sales";
            }
        });



        SupplierRole roleEditor = FactoryBoy.create(SupplierRole.class, new BuildCallback<SupplierRole>() {
            @Override
            public void build(SupplierRole role) {
                role.text = "编辑";
                role.key = "editor";
            }
        });



        SupplierRole roleAccount = FactoryBoy.create(SupplierRole.class, new BuildCallback<SupplierRole>() {
            @Override
            public void build(SupplierRole role) {
                role.text = "财务";
                role.key = "account";
            }
        });

        supplierUser = FactoryBoy.create(SupplierUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUser.loginName);
    }

    @Test
    public void testIndex() throws Exception {
        Response response = GET("/profile");
        assertIsOk(response);
        SupplierUser user = (SupplierUser) renderArgs("supplierUser");
        assertEquals(supplierUser.id, user.id);
        String roleIds = (String) renderArgs("roleIds");
        assertNotNull(roleIds);
    }

    @Test
    public void testUpdate() throws Exception {
        Map<String, String> form = new HashMap<>();
        form.put("supplierUser.mobile", "15026682165");
        form.put("supplierUser.jobNumber", "9527");
        form.put("supplierUser.loginName", "tom");
        Response response = POST("/profile/1", form);
        assertStatus(302, response);

        supplierUser.refresh();
        assertEquals("9527", supplierUser.jobNumber);
    }

    @Test
    public void testCheckValidError() throws Exception {
        Map<String, String> form = new HashMap<>();
        form.put("supplierUser.id", supplierUser.id.toString());
        form.put("supplierUser.mobile", "15026682165");
        form.put("supplierUser.jobNumber", "abc");
        form.put("supplierUser.loginName", "tom");
        Response response = POST("/profile/"+supplierUser.id, form);
        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("validation.jobNumber", errors.get(0).getKey());
        assertStatus(200, response);
    }

}
