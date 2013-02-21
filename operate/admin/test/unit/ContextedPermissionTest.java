package unit;

import factory.FactoryBoy;
import models.operator.OperateUser;
import operate.rbac.ContextedPermission;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import play.vfs.VirtualFile;

public class ContextedPermissionTest extends UnitTest {

    OperateUser operateUser;
	@Before
	public void setup() {
        FactoryBoy.deleteAll();

        // 加载test/rbac.xml配置文件
        VirtualFile file = VirtualFile.open("test/rbac.xml");
        RbacLoader.init(file);

        operateUser = FactoryBoy.create(OperateUser.class);
        operateUser.refresh();
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
		ContextedPermission.init(operateUser);

        assertFalse(ContextedPermission.hasPermission("NO_ADMIN"));
    }


    @Test
    public void testTestUserHasPermission() {
		ContextedPermission.init(operateUser);

		assertEquals(4, ContextedPermission.getAllowPermissions().size());

		assertTrue(ContextedPermission.hasPermission("USER"));
	}
}
