package functional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.consumer.User;
import models.consumer.UserInfo;

import org.junit.Before;
import org.junit.Test;

import play.Play;
import play.cache.Cache;
import play.data.validation.Validation;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

public class RegisterTest extends FunctionalTest {
	@Before
	public void setup() {
		Fixtures.delete(User.class);
		Fixtures.delete(UserInfo.class);
		Fixtures.loadModels("fixture/user.yml");
		
	}
	
	@Test
	public void testCreate() {
		List old = User.findAll();
		int count = old.size();
		
		Map<String, String> loginUserParams = new HashMap<String,String>();
		loginUserParams.put("user.loginName", "11@qq.com");
		loginUserParams.put("user.password", "123456");
		loginUserParams.put("user.confirmPassword", "123456");
		loginUserParams.put("user.captcha", "A2WQ");
		loginUserParams.put("randomID", "RANDOMID");
		Cache.set("RANDOMID", "A2WQ","30mn");
		Response response = POST("/register", loginUserParams);
		assertStatus(200,response);

		List newList = User.findAll();
		assertEquals(count+1,newList.size());
		
		List userInfos = UserInfo.findAll();
		//assertEquals(1,userInfos.size());
		assertEquals(2,userInfos.size());

		loginUserParams.put("user.loginName", "11@qq.com");
		loginUserParams.put("user.password", "123456");
		loginUserParams.put("user.confirmPassword", "1");
		loginUserParams.put("user.captcha", "A2WQ");
		loginUserParams.put("randomID", "RANDOMID");
		Cache.set("RANDOMID", "AAAA","30mn");
		response = POST("/register", loginUserParams);
		assertStatus(200,response);
	}
	

    @Test
    public void testCheckLoginName() {
    	Map<String, String> loginUserParams = new HashMap<String,
				String>();
		loginUserParams.put("user.loginName", "11@qq.com");
		loginUserParams.put("user.mobile", "1313112112");
    	Response response = POST("/register/checkLoginName", loginUserParams);
		assertStatus(200,response);
        assertCharset(Play.defaultWebEncoding, response);
    }
    
    @Test
	public void testCreateUserError() {
    	Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
		User user = User.findById(userId);
		
		Map<String,String> userParams=new HashMap<String,String>();
		userParams.put("user.mobile","123");
		userParams.put("user.loginName", "11@qq.com");
		userParams.put("user.password", "123456");
		userParams.put("user.confirmPassword", "123456");
		userParams.put("user.captcha", "A2WQ");
		userParams.put("randomID", "RANDOMID");
		Cache.set("RANDOMID", "A2WQ","30mn");
		//user.mobile="123";
		//user.save();
		//System.out.println(user.mobile);
	
		Response response = POST("/register", userParams);
		assertEquals(0, Validation.errors().size());
		 
    	
    }
    @Test
   	public void testCaptcha() {
    	Response response = GET("/captcha");
		assertStatus(200, response); 
		
    }
	

}
