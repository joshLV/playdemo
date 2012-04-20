import java.util.HashMap;
import java.util.Map;

import models.consumer.User;
import models.consumer.UserInfo;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import play.cache.Cache;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

@Ignore
public class UserInfoTest extends FunctionalTest {
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
	public void testUpdate() {
		Long userInfoId = (Long) Fixtures.idCache.get("models.consumer.UserInfo-userInfo1");

		String intrests = "1,2";
		Map<String, String> params = new HashMap<String,
				String>();
		params.put("userInfo.fullName", "小小");
		params.put("userInfo.birthdayYear", "2003");
		params.put("userInfo.birthdayMonth", "06");
		params.put("userInfo.birthdayDay", "12");
		params.put("id", userInfoId.toString());
		params.put("intrests", intrests);
		Response response = POST("/userInfo/update", params);
		assertStatus(302,response);

		UserInfo userInfo= UserInfo.findById(userInfoId);
		assertEquals("小小",userInfo.fullName);
		assertEquals("20030612",userInfo.birthday);
	}


	@Test
	public void testBindMobile() {
		Long userInfoId = (Long) Fixtures.idCache.get("models.consumer.UserInfo-userInfo1");

		//保存手机和验证码
		Cache.set("validCode_", "2003", "10mn");
		Cache.set("mobile_", "15912567896", "10mn");
		
		Map<String, String> params = new HashMap<String,
				String>();
		params.put("mobile", "15912567896");
		params.put("validCode", "2003");
		Response response = POST("/userInfo/bindMobile", params);
		assertStatus(302,response);

		UserInfo userInfo= UserInfo.findById(userInfoId);
		assertEquals("15912567896",userInfo.mobile);

		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-selenium");

		User user= User.findById(userId);
		assertEquals("15912567896",user.mobile);
	}
}
