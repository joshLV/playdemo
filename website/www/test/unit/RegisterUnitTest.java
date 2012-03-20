package unit;

import java.util.Date;
import java.util.List;

import models.consumer.User;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Test;

import play.libs.Images;
import play.test.UnitTest;

public class RegisterUnitTest extends UnitTest {
	@Test
	public void registersTest() { 
		User user= new User();
		user.mobile=	"13000000000";
		user.loginName="admin@qq.com";
		Images.Captcha captcha = Images.captcha();
		String passwordSalt=captcha.getText(6);
		//密码加密
		user.password=DigestUtils.md5Hex("1"+passwordSalt);
		//正常
		user.status=1;
		//随机吗
		user.passwordSalt=passwordSalt;
		user.loginIp="127.0.0.1";
		user.lastLoginAt = new Date();
		user.createdAt = new Date();
		user.save();

		List<User> list = User.findAll(); 

		Assert.assertNotNull(list);  
		Assert.assertTrue(list.size() !=0);  
		Assert.assertEquals("admin@qq.com", list.get(0).loginName);  
	}  
}
