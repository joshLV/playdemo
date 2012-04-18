package unit;

import java.util.Date;
import java.util.List;

import models.consumer.User;
import models.resale.AccountType;
import models.resale.Resaler;
import models.resale.ResalerStatus;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import play.libs.Images;
import play.test.Fixtures;
import play.test.UnitTest;
public class ResalerInfosUnitTest extends UnitTest {
	@Before
	public void setup() {
		Fixtures.delete(Resaler.class);
		Fixtures.loadModels("fixture/resaler.yml");
	}

	@Test
	public void registersTest() { 
		List<Resaler> list = Resaler.findAll(); 
		int cnt =list.size();
		Resaler resaler = new Resaler();
		resaler.loginName = "yyyy";
		resaler.mobile = "13000000001";
		Images.Captcha captcha = Images.captcha();
		String passwordSalt = captcha.getText(6);
		//密码加密
		resaler.password = DigestUtils.md5Hex("1"+passwordSalt);
		resaler.confirmPassword = "1";
		resaler.userName = "小李";
		//正常
		resaler.status = ResalerStatus.PENDING;
		//随机码
		resaler.passwordSalt = passwordSalt;
		resaler.address="上海市";
		resaler.accountType=AccountType.COMPANY;
		resaler.email="11@qq.com";
		resaler.loginIp = "127.0.0.1";
		resaler.lastLoginAt = new Date();
		resaler.createdAt = new Date();
		resaler.save();

		list = Resaler.findAll(); 
		assertEquals(cnt+1, list.size());  
	}  

	//测试是否存在用户名和手机
	@Test
	public void testCheckValue(){
		String returnFlag = Resaler.checkValue("jane", "");
		assertEquals("1",returnFlag); 

		returnFlag = Resaler.checkValue("dd", "13213123124");
		assertEquals("2",returnFlag); 

		returnFlag = Resaler.checkValue("ee", "13213123125");
		assertEquals("0",returnFlag);
	}
}
