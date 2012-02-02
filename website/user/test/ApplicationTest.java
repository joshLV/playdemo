import java.util.HashMap;
import java.util.Map;

import models.Registers;

import org.junit.Test;

import play.cache.Cache;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import controllers.Register;

public class ApplicationTest extends FunctionalTest {

	@Test
	public void testCreat() {
		// Make the login request
		Map<String, String> loginUserParams = new HashMap<String,
				String>();
		loginUserParams.put("email", "11@qq.com");
		loginUserParams.put("mobile", "1313112112");
		loginUserParams.put("password", "123456");
		loginUserParams.put("sure_pwd", "123456");
		loginUserParams.put("captcha", "A2WQ");
		loginUserParams.put("randomID", "RANDOMID");
		Cache.set("RANDOMID", "A2WQ","30mn");
		Response response = POST("/register", loginUserParams);
		assertStatus(302,response);

		loginUserParams.put("email", "11");
		loginUserParams.put("mobile", "1313112112");
		response = POST("/register", loginUserParams);
		assertStatus(200,response);


		loginUserParams.put("email", "11@qq.com");
		loginUserParams.put("mobile", "131312");
		response = POST("/register", loginUserParams);
		assertStatus(200,response);


		loginUserParams.put("email", "11@qq.com");
		loginUserParams.put("mobile", "1313112112");
		loginUserParams.put("password", "123456");
		loginUserParams.put("sure_pwd", "126");
		response = POST("/register", loginUserParams);
		assertStatus(200,response);


		loginUserParams.put("email", "11@qq.com");
		loginUserParams.put("mobile", "1313112112");
		loginUserParams.put("password", "123456");
		loginUserParams.put("sure_pwd", "123456");
		loginUserParams.put("captcha", "");

		response = POST("/register", loginUserParams);
		assertStatus(200,response);


		loginUserParams.put("email", "11@qq.com");
		loginUserParams.put("mobile", "1313112112");
		loginUserParams.put("password", "123456");
		loginUserParams.put("sure_pwd", "123456");
		loginUserParams.put("captcha", "A2WQ");
		loginUserParams.put("randomID", "RANDOMID");
		Cache.set("RANDOMID", "AAAA","30mn");
		response = POST("/register", loginUserParams);
		assertStatus(200,response);
	}
}