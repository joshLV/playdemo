package unit;

import models.admin.SupplierRole;
import models.admin.SupplierUser;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class PermissionHandlerTest extends UnitTest {

	@Before
	public void setup() {
		Fixtures.delete(SupplierUser.class);
		Fixtures.delete(SupplierRole.class);
		Fixtures.loadModels("fixture/roles.yml");
		Fixtures.loadModels("fixture/cusers.yml");
	}
	
	@Test
	public void testHasPermission() {
	    
		Long id = (Long) Fixtures.idCache.get("models.admin.SupplierUser-user3");
	}
	
	
}
