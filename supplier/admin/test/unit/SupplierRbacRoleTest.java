package unit;

import factory.FactoryBoy;
import models.admin.SupplierNavigation;
import models.admin.SupplierRole;
import navigation.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import play.vfs.VirtualFile;

import java.util.List;

public class SupplierRbacRoleTest extends UnitTest {

    @Before
    @SuppressWarnings("unchecked")
    public void setupDatabase() {
        FactoryBoy.deleteAll();
        FactoryBoy.create(SupplierNavigation.class);
        // 加载test/rbac.xml配置文件
        VirtualFile file = VirtualFile.open("test/rbac.xml");
        RbacLoader.init(file);
    }

    @Test
    public void canLoadRoleToDB() {
        // 加载后，数据库中必须有相关的记录
        List<SupplierRole> roles = SupplierRole.findAll();
        assertTrue(roles.size() > 0);

        SupplierRole adminRole = SupplierRole.find("byKey", "admin")
                .first();
        assertNotNull(adminRole);
        assertEquals("管理员", adminRole.text);
    }

}
