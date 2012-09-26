package unit;

import factory.FactoryBoy;
import models.consumer.User;
import models.consumer.UserInfo;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.List;

public class RegisterUnitTest extends UnitTest {
    User user;
    UserInfo userInfo;
    @Before
	public void setup() {
        FactoryBoy.delete(User.class);
        FactoryBoy.delete(UserInfo.class);
        userInfo=FactoryBoy.create(UserInfo.class);
        user = FactoryBoy.create(User.class);
	}

	@Test
	public void registersTest() { 
		User user = new User();
		user.loginName = "11@qq.com";
		user.create();
		List<User> list = User.findAll(); 
		assertEquals(2, list.size());
	}  


	//测试是否存在用户名和手机
	@Test
	public void testCheckValue(){
		boolean returnFlag = User.checkLoginName("11@qq.com");
		assertFalse(returnFlag);

		returnFlag = User.checkLoginName("selenium@uhuila.com");
		assertTrue("1",returnFlag);
	}

	@Test
	public void testUpdatePassword(){
		Long userId = user.id;
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
        Long userId = user.id;
		User user = User.findById(userId);
		String mobile = "13518096999";
		user.updateMobile(mobile);
		User newUser = User.findById(userId);
		assertEquals(mobile,newUser.mobile);
	}
}
