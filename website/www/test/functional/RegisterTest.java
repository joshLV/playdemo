package functional;

import org.junit.Test;
import play.cache.Cache;
import play.mvc.Http.Response;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;

public class RegisterTest extends FunctionalTest {

	@Test
	public void testCreat() {
		// Make the login request
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
		assertStatus(302,response);

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
		assertStatus(302,response);


		loginUserParams.put("user.loginName", "11@qq.com");
		loginUserParams.put("user.mobile", "1313112112");
		loginUserParams.put("user.password", "123456");
		loginUserParams.put("user.confirmPassword", "123456");
		loginUserParams.put("user.captcha", "A2WQ");
		loginUserParams.put("randomID", "RANDOMID");
		Cache.set("RANDOMID", "AAAA","30mn");
		response = POST("/register", loginUserParams);
		assertStatus(302,response);
	}

}
