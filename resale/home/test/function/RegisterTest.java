package function;

import java.util.HashMap;
import java.util.Map;

import models.resale.AccountType;
import models.resale.Resaler;
import models.resale.ResalerStatus;

import org.junit.Before;
import org.junit.Test;

import play.cache.Cache;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

public class RegisterTest extends FunctionalTest {
	@Before
	public void setup() {
		Fixtures.delete(Resaler.class);
		Fixtures.loadModels("fixture/resaler.yml");
	}

	@Test
	public void testCreat() {
		Map<String, String> loginUserParams = new HashMap<String,
				String>();
		loginUserParams.put("resaler.loginName", "qqq");
		loginUserParams.put("resaler.mobile", "13131121121");
		loginUserParams.put("resaler.password", "123456");
		loginUserParams.put("resaler.confirmPassword", "123456");
		loginUserParams.put("resaler.address", "上海市");
		loginUserParams.put("resaler.email", "11@qq.com");
		loginUserParams.put("resaler.accountType", AccountType.COMPANY.toString());
		//正常
		loginUserParams.put("resaler.status", ResalerStatus.NOMAL.toString());
		loginUserParams.put("resaler.confirmPassword", "123456");
		loginUserParams.put("resaler.phone", "0213212121");
		loginUserParams.put("resaler.userName", "小李");
		loginUserParams.put("resaler.identityNo", "341281198208268785");
		Response response = POST("/register", loginUserParams);
		assertStatus(302,response);

		//异常情况
		loginUserParams.put("resaler.loginName", "rrrr");
		loginUserParams.put("resaler.mobile", "13131121123");
		loginUserParams.put("resaler.password", "123456");
		loginUserParams.put("resaler.confirmPassword", "126");
		response = POST("/register", loginUserParams);
		assertStatus(200,response);


		loginUserParams.put("resaler.loginName", "t");
		loginUserParams.put("resaler.mobile", "13131121141");
		loginUserParams.put("resaler.password", "123456");
		loginUserParams.put("resaler.confirmPassword", "123456");
		loginUserParams.put("resaler.address", "上海市");
		loginUserParams.put("resaler.email", "11@qq.com");
		loginUserParams.put("resaler.accountType", AccountType.COMPANY.toString());
		//正常
		loginUserParams.put("resaler.status", ResalerStatus.NOMAL.toString());
		loginUserParams.put("resaler.identityNo","341281198208268785");
		response = POST("/register", loginUserParams);
		assertStatus(200,response);

	}
	
	//测试是否存在用户名和手机
	@Test
	public void testCheckValue(){
		Map<String, String> loginUserParams = new HashMap<String,
				String>();
		loginUserParams.put("resaler.loginName", "qqq");
		loginUserParams.put("resaler.mobile", "13131121121");
		
		Response response = POST("/register/checkLoginName", loginUserParams);
		assertStatus(200,response);
		
	}

}
