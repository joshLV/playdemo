package function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.admin.SupplierRole;
import models.admin.SupplierUser;
import navigation.RbacLoader;

import org.junit.After;
import org.junit.Test;

import com.uhuila.common.constants.DeletedStatus;

import play.Play;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;

public class SupplierUsersTest extends FunctionalTest {
    SupplierUser supplierUser;

    @org.junit.Before
    public void setup() {
        FactoryBoy.deleteAll();
        supplierUser = FactoryBoy.create(SupplierUser.class);
    }


    /**
     * 查看操作员信息
     */
    @Test
    public void testIndex() {
        Response response = GET("/users");
        assertStatus(302, response);
    }

    //测试是否存在用户名和手机
    @Test
    public void testCheckValue() {
        Map<String, String> params = new HashMap<>();
        params.put("loginName", "test");
        params.put("mobile", "1300000001");
        params.put("id", supplierUser.id.toString());
        Response response = POST("/users/checkLoginName", params);
        assertStatus(302, response);
    }
}
