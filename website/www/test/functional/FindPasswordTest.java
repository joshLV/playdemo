/**
 * 
 */
package functional;

import java.util.HashMap;
import java.util.Map;

import models.consumer.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import play.cache.Cache;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import controllers.modules.website.cas.Security;

/**
 * @author wangjia
 * @date 2012-7-31 下午1:14:50 
 */
public class FindPasswordTest extends FunctionalTest{

	
	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		Fixtures.delete(User.class);
		Fixtures.loadModels("fixture/user.yml");

		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
		User user = User.findById(userId);

		// 设置测试登录的用户名
		Security.setLoginUserForTest(user.loginName);
		Cache.delete("mobile_");
	}

	@After
	public void tearDown() {
		// 清除登录Mock
		Security.cleanLoginUserForTest();
	}

	@Test
	public void testSendMessageCode() {
		Response response = GET("/send-message_code?from=15618096151");
		assertStatus(200, response);
		assertNotNull(response); // this is OK
		assertIsOk(response); // this is OK 200
		assertContentType("application/json", response); // this is OK
		assertCharset("utf-8", response); // this is OK 
		assertEquals("1", response.out.toString()); // 浏览器相应
	}
	
	@Test
	public void testCheckMobileMobileNotExist() {
		Response response = GET("/check-mobile");
		assertStatus(200, response);
		assertNotNull(response); // this is OK
		assertIsOk(response); // this is OK 200
		assertContentType("application/json", response); // this is OK
		assertCharset("utf-8", response); // this is OK 
		assertEquals("3", response.out.toString()); // 浏览器相应	
	}
	
	
	@Test
	public void testCheckMobileValidCodeWrong() {
		Response response = GET("/check-mobile?mobile=15618096151&validCode=123");
		assertStatus(200, response);
		assertNotNull(response); // this is OK
		assertIsOk(response); // this is OK 200
		assertContentType("application/json", response); // this is OK
		assertCharset("utf-8", response); // this is OK 
		assertEquals("1", response.out.toString()); // 浏览器相应	
	}
	
	
	
	
	
	@Test
	public void testCheckMobile() {
		Cache.set("mobile_", "15618096151","30mn");
		Response response = GET("/check-mobile?mobile=15618096151&validCode=123456");
		assertStatus(200, response);
		assertNotNull(response); // this is OK
		assertIsOk(response); // this is OK 200
		assertContentType("application/json", response); // this is OK
		assertCharset("utf-8", response); // this is OK 	
		assertEquals("0", response.out.toString()); // 浏览器相应	
	}
	
	@Test
	public void testUpdatePassword() {
		
		Map<String, String> passwordParams = new HashMap<>();
		passwordParams.put("password","123");
		passwordParams.put("confirmPassword","123");
		Response response = POST("/reset-password", passwordParams);
		assertEquals("-1", response.out.toString()); // 浏览器相应	
	}
	
	
	@Test
	public void testUpdatePasswordMobileBlank() {
		Map<String, String> passwordParams = new HashMap<>();
		passwordParams.put("mobile","15618096151");
		passwordParams.put("password","123");
		passwordParams.put("confirmPassword","123");
		Response response = POST("/reset-password", passwordParams);
		assertEquals("1", response.out.toString()); // 浏览器相应	
	}
	
	
	@Test  
	public void testCheckMobilePhoneWrong() {
		Cache.set("mobile_", "123","30mn");
		Response response = GET("/check-mobile?mobile=15618096151&validCode=123456");
		assertStatus(200, response);
		assertNotNull(response); // this is OK
		assertIsOk(response); // this is OK 200
		assertContentType("application/json", response); // this is OK
		assertCharset("utf-8", response); // this is OK 
		assertEquals("2", response.out.toString()); // 浏览器相应	

	}

}
