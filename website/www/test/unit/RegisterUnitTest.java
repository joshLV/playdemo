package unit;

import models.consumer.User;
import models.consumer.UserStatus;
import models.resale.Resaler;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.libs.Images;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.Date;
import java.util.List;

public class RegisterUnitTest extends UnitTest {
	@Before
	public void setup() {
		Fixtures.delete(User.class);
		Fixtures.loadModels("fixture/user.yml");
	}

	@Test
	public void registersTest() { 
		User user = new User();
		user.loginName = "11@qq.com";
		user.create();

		List<User> list = User.findAll(); 
		assertEquals(4, list.size());  
	}  


	//测试是否存在用户名和手机
	@Test
	public void testCheckValue(){
		String returnFlag = User.checkLoginName("11@qq.com");
		assertEquals("0",returnFlag); 

		returnFlag = User.checkLoginName("selenium@uhuila.com");
		assertEquals("1",returnFlag);
	}

	@Test
	public void testUpdatePassword(){
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
		User user = new User();
		user.password="654321";
		User newUser = User.findById(userId);
		user.updatePassword(newUser, user);
		
		User newUser1 = User.findById(userId);
		String password = DigestUtils.md5Hex("654321"+newUser1.passwordSalt);
		assertEquals(password,newUser1.password);
	}
	
	@Test
	public void testUpdateMobile(){
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
		User user = User.findById(userId);
		String mobile = "15618096999";
		user.updateMobile(mobile);
		User newUser = User.findById(userId);
		assertEquals(mobile,newUser.mobile);
	}
}
