package unit;

import java.util.List;

import models.admin.OperateNavigation;
import models.admin.OperateRole;
import operate.rbac.RbacLoader;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import play.vfs.VirtualFile;
import factory.FactoryBoy;

public class RbacRoleTest extends UnitTest {

    @Before
    public void setupDatabase() {
        FactoryBoy.deleteAll();
        FactoryBoy.create(OperateNavigation.class);
        // 加载test/rbac.xml配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
    }
    
    @Test
    public void canLoadRoleToDB() {
        // 加载后，数据库中必须有相关的记录
        List<OperateRole> roles = OperateRole.findAll();
        assertTrue(roles.size() > 0);

        OperateRole adminRole = OperateRole.find("byKey", "admin")
                .first();
        assertNotNull(adminRole);
        assertEquals("系统管理员", adminRole.text);
    }

}
