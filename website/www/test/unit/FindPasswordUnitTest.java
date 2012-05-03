package unit;

import models.consumer.User;
import models.consumer.UserInfo;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
public class FindPasswordUnitTest extends UnitTest {
	@Before
	public void setup() {
		Fixtures.delete(User.class);
		Fixtures.loadModels("fixture/user.yml");
	}
	
	@Test
	public void getUser(){
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user_test1");
		User user= User.findById(userId);
		String loginName ="selenium1@uhuila.com";
		String returnFlag = user.getUser(loginName);
		assertEquals("1",returnFlag);
	}
	
	@Test
	public void checkMobile(){
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user_test1");
		User user= User.findById(userId);
		String mobile ="15618096151";
		String returnFlag = user.checkMobile(mobile);
		assertEquals("2",returnFlag);
	}
	
	@Test
	public void testUpdateFindPwd(){
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user_test1");
		User user= User.findById(userId);

		String totken ="63dc778349e8f64e7c1c3b9370848ba2";
		String password = "654321";
		user.updateFindPwd(totken, "", "654321");
		user= User.findById(userId);
		assertEquals(DigestUtils.md5Hex(password + user.passwordSalt),user.password);
		
		String mobile = "15618096151";
		user.updateFindPwd("", mobile, "654321");
		user= User.findById(userId);
		assertEquals(DigestUtils.md5Hex(password + user.passwordSalt),user.password);
	}

	@Test
	public void testIsExpired(){
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user_test2");
		User user= User.findById(userId);
		String totken ="63dc778349e8f64e7c1c3b9370848ba1";
		boolean isExpired  = user.isExpired(totken);
		assertTrue(isExpired);
	}

}
