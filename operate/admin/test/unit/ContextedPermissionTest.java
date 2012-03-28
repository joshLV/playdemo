package unit;

import models.admin.OperateRole;
import models.admin.OperateUser;
import operate.rbac.ContextedPermission;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;
import play.vfs.VirtualFile;

public class ContextedPermissionTest extends UnitTest {

    @Before
    public void setup() {
        Fixtures.delete(OperateUser.class);
        Fixtures.delete(OperateRole.class);
        Fixtures.loadModels("fixture/roles.yml");
        Fixtures.loadModels("fixture/cusers.yml");

        // 加载test/rbac.xml配置文件
        VirtualFile file = VirtualFile.open("test/rbac.xml");
        RbacLoader.init(file);
    }

    @After
    public void tearDown() {
        ContextedPermission.clean();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
    }

    @Test
    public void testNotExistsUser() {
        ContextedPermission.init(null); // not exists user
        assertFalse(ContextedPermission.hasPermission("XXX"));
    }

    @Test
    public void testAdminUserHasNotPermission() {
        Long id = (Long) Fixtures.idCache.get("models.admin.OperateUser-user3");
        OperateUser user = OperateUser.findById(id);
        ContextedPermission.init(user);

        assertFalse(ContextedPermission.hasPermission("PERM_TEST"));
    }


    @Test
    public void testTestUserHasPermission() {
        Long id = (Long) Fixtures.idCache.get("models.admin.OperateUser-user2");
        OperateUser user = OperateUser.findById(id);
        ContextedPermission.init(user);

        assertEquals(1, ContextedPermission.getAllowPermissions().size());

        assertTrue(ContextedPermission.hasPermission("PERM_TEST"));
    }
}
