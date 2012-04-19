package unit;

import models.consumer.User;
import models.consumer.UserStatus;

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
	}

	@Test
	public void registersTest() { 
		User user = new User();
		user.mobile = "13000000001";
		user.loginName = "11@qq.com";
		Images.Captcha captcha = Images.captcha();
		String passwordSalt = captcha.getText(6);
		//密码加密
		user.password = DigestUtils.md5Hex("1"+passwordSalt);
		user.confirmPassword = "1";
		//正常
		user.status = UserStatus.NORMAL;
		//随机码
		user.passwordSalt = passwordSalt;
		user.captcha = "awwr";
		user.loginIp = "127.0.0.1";
		user.lastLoginAt = new Date();
		user.createdAt = new Date();
		user.save();

		List<User> list = User.findAll(); 
		Assert.assertNotNull(list);  
		Assert.assertTrue(list.size() !=0);  
		Assert.assertEquals("11@qq.com", list.get(0).loginName);  
	}  
}
