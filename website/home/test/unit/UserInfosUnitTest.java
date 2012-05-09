package unit;

import models.consumer.User;
import models.consumer.UserInfo;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
public class UserInfosUnitTest extends UnitTest {
	@Before
	public void setup() {
		Fixtures.delete(User.class);
		Fixtures.delete(UserInfo.class);
		Fixtures.loadModels("fixture/user.yml");
		Fixtures.loadModels("fixture/userInfo.yml");
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-selenium");

		User user= User.findById(userId);

		Long userInfoId = (Long) Fixtures.idCache.get("models.consumer.UserInfo-userInfo1");
		UserInfo userInfo= UserInfo.findById(userInfoId);

		userInfo.user = user;
		userInfo.save();
	}

	@Test
	public void testUpdateMobile(){
		Long userInfoId = (Long) Fixtures.idCache.get("models.consumer.UserInfo-userInfo1");
		UserInfo userInfo= UserInfo.findById(userInfoId);

		UserInfo userInfo1= new UserInfo();
		userInfo1.fullName="小小";
		userInfo1.birthday="2001-01-01";
		String interests = "1,2";

		userInfo.update(userInfo1, interests);

		assertEquals("小小",userInfo.fullName);
		assertEquals("2001-01-01",userInfo.birthday);
	}

	@Test
	public void testUpdateById(){
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-selenium");
		User user= User.findById(userId);

		Long userInfoId = (Long) Fixtures.idCache.get("models.consumer.UserInfo-userInfo1");
		UserInfo userInfo= UserInfo.findById(userInfoId);
		userInfo.user = user;
		String mobile = "15200000012";
		userInfo.updateById(user, mobile);

		assertEquals("15200000012",userInfo.mobile);
	}

	@Test
	public void testFindByUser(){
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-selenium");
		User user= User.findById(userId);

		Long userInfoId = (Long) Fixtures.idCache.get("models.consumer.UserInfo-userInfo1");
		UserInfo userInfo= UserInfo.findById(userInfoId);
		userInfo.findByUser(user);

		assertEquals("2001-01-01",userInfo.birthday);
	}

}
