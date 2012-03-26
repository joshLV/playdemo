package unit;

import models.admin.SupplierRole;
import models.admin.SupplierUser;
import navigation.ContextedPermission;
import navigation.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;
import play.vfs.VirtualFile;

public class ContextedPermissionTest extends UnitTest {

	@Before
	public void setup() {
		Fixtures.delete(SupplierUser.class);
		Fixtures.delete(SupplierRole.class);
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
	    ContextedPermission.init("notExistsUser");
	    assertFalse(ContextedPermission.hasPermission("XXX"));
	}
	
	@Test
	public void testAdminUserHasNotPermission() {
		Long id = (Long) Fixtures.idCache.get("models.admin.SupplierUser-user3");
		SupplierUser user = SupplierUser.findById(id);
		ContextedPermission.init(user.loginName);
		
		assertFalse(ContextedPermission.hasPermission("PERM_TEST"));
	}
	
	
	@Test
	public void testTestUserHasPermission() {
		Long id = (Long) Fixtures.idCache.get("models.admin.SupplierUser-user2");
		SupplierUser user = SupplierUser.findById(id);
		ContextedPermission.init(user.loginName);
		
		assertEquals(1, ContextedPermission.getAllowPermissions().size());
		
		assertTrue(ContextedPermission.hasPermission("PERM_TEST"));
	}
}
