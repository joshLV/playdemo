package unit;

import java.util.List;

import models.admin.SupplierPermission;

import org.junit.Test;

import play.test.UnitTest;

public class RbacPermissionTest extends UnitTest {

    @Test
    public void canLoadPermissionYamlFile() {
        // 加载后，数据库中必须有相关的记录
        List<SupplierPermission> perms = SupplierPermission.find("byApplicationName", "traders-admin").fetch();
        assertTrue(perms.size() > 0);

        SupplierPermission user = SupplierPermission.find("byApplicationNameAndKey", "traders-admin", "USER")
                .first();
        assertNotNull(user);
        SupplierPermission userAdd = SupplierPermission.find("byApplicationNameAndKey", "traders-admin", "USER_ADD")
                .first();
        assertNotNull(userAdd);
        assertNotNull(userAdd.parent);
        assertEquals(user.text, userAdd.parent.text);

    }


}
