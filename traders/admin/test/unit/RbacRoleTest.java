package unit;

import java.util.List;

import models.admin.SupplierNavigation;
import models.admin.SupplierRole;

import navigation.ContextedMenu;
import navigation.RbacLoader;

import org.junit.Test;

import play.test.UnitTest;

public class RbacRoleTest extends UnitTest {

    @Test
    public void canLoadRoleToDB() {
        // 加载后，数据库中必须有相关的记录
        List<SupplierRole> roles = SupplierRole.findAll();
        assertTrue(roles.size() > 0);

        SupplierRole adminRole = SupplierRole.find("byKey", "admin")
                .first();
        assertNotNull(adminRole);
        assertEquals("系统管理员", adminRole.text);
    }

}
