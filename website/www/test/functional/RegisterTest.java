package functional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.consumer.User;
import models.resale.Resaler;

import org.junit.Before;
import org.junit.Test;

import play.cache.Cache;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

public class RegisterTest extends FunctionalTest {
	@Before
	public void setup() {
		Fixtures.delete(User.class);
	}
	
	@Test
	public void testCreat() {
		List old = User.findAll();
		int count = old.size();
		Map<String, String> loginUserParams = new HashMap<String,
				String>();
		loginUserParams.put("user.loginName", "11@qq.com");
		loginUserParams.put("user.mobile", "1313112112");
		loginUserParams.put("user.password", "123456");
		loginUserParams.put("user.confirmPassword", "123456");
		loginUserParams.put("user.captcha", "A2WQ");
		loginUserParams.put("randomID", "RANDOMID");
		Cache.set("RANDOMID", "A2WQ","30mn");
		Response response = POST("/register", loginUserParams);
		assertStatus(200,response);

		List newList = User.findAll();
		assertEquals(count+1,newList.size());
		
		loginUserParams.put("user.loginName", "11");
		loginUserParams.put("user.mobile", "1313112112");
		response = POST("/register", loginUserParams);
		assertStatus(200,response);


		loginUserParams.put("user.loginName", "11@qq.com");
		loginUserParams.put("user.mobile", "131312");
		response = POST("/register", loginUserParams);
		assertStatus(200,response);


		loginUserParams.put("user.loginName", "11@qq.com");
		loginUserParams.put("user.mobile", "1313112112");
		loginUserParams.put("user.password", "123456");
		loginUserParams.put("user.confirmPassword", "126");
		response = POST("/register", loginUserParams);
		assertStatus(200,response);


		loginUserParams.put("user.loginName", "11@qq.com");
		loginUserParams.put("user.mobile", "1313112112");
		loginUserParams.put("user.password", "123456");
		loginUserParams.put("user.confirmPassword", "123456");
		loginUserParams.put("captcha", "");

		response = POST("/register", loginUserParams);
		assertStatus(200,response);


		loginUserParams.put("user.loginName", "11@qq.com");
		loginUserParams.put("user.mobile", "1313112112");
		loginUserParams.put("user.password", "123456");
		loginUserParams.put("user.confirmPassword", "1");
		loginUserParams.put("user.captcha", "A2WQ");
		loginUserParams.put("randomID", "RANDOMID");
		Cache.set("RANDOMID", "AAAA","30mn");
		response = POST("/register", loginUserParams);
		assertStatus(200,response);
		
	}

}
