package unit;

import models.consumer.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;
public class UserUnitTest extends UnitTest {
	@Before
	public void setup() {
		Fixtures.delete(User.class);
		Fixtures.loadModels("fixture/user.yml");
	}
	
	@Test
	public void passwordTest() { 
	
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-selenium");
		User user =new User();
		user.password="654321";
		User newUser =User.findById(userId);
		String password = newUser.password;
		
		user.updatePassword(newUser, user);
		User upduser =User.findById(userId);
		assertNotSame(password, upduser.password);  
		assertEquals( DigestUtils.md5Hex("654321"+upduser.passwordSalt), upduser.password);  
	} 
}
