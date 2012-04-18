package function;

import models.admin.SupplierRole;
import models.admin.SupplierUser;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
public class SupplierPasswordUnitTest extends UnitTest {
	@Before
	public void setup() {
		Fixtures.delete(SupplierUser.class);
		Fixtures.delete(SupplierRole.class);
		Fixtures.loadModels("fixture/roles.yml");
		Fixtures.loadModels("fixture/supplier_users.yml");
	}

	@Test
	public void passwordTest() { 
	
		Long resalerId = (Long) Fixtures.idCache.get("models.admin.SupplierUser-user1");
		SupplierUser supplier =new SupplierUser();
		supplier.encryptedPassword="654321";
		SupplierUser newSupplier =SupplierUser.findById(resalerId);
		String password = newSupplier.encryptedPassword;
		
		supplier.updatePassword(newSupplier, supplier);
		SupplierUser updresaler =SupplierUser.findById(resalerId);
		assertNotSame(password, updresaler.encryptedPassword);  
		assertEquals( DigestUtils.md5Hex("654321"+updresaler.passwordSalt), updresaler.encryptedPassword);  
	} 
}
