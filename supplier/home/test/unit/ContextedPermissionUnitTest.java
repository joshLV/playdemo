package unit;

import factory.FactoryBoy;
import models.admin.SupplierUser;
import navigation.ContextedPermission;
import navigation.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import play.vfs.VirtualFile;

public class ContextedPermissionUnitTest extends UnitTest {

    SupplierUser supplierUser;
	@Before
	public void setup() {
        FactoryBoy.deleteAll();

        // 加载test/rbac.xml配置文件
        VirtualFile file = VirtualFile.open("test/rbac.xml");
        RbacLoader.init(file);

        supplierUser = FactoryBoy.create(SupplierUser.class);
        supplierUser.refresh();
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
		ContextedPermission.init(supplierUser);
		
		assertFalse(ContextedPermission.hasPermission("USER"));
	}
	
	
	@Test
	public void testTestUserHasPermission() {
		ContextedPermission.init(supplierUser);
		
		assertEquals(2, ContextedPermission.getAllowPermissions().size());
		
		assertTrue(ContextedPermission.hasPermission("USER_ADD"));
	}
}
