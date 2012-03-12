package function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.admin.SupplierRole;
import models.admin.SupplierUser;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Test;

import play.Play;
import play.libs.Images;
import play.mvc.Http;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

import com.uhuila.common.constants.DeletedStatus;

public class CuserFunctionTest extends FunctionalTest {
	@org.junit.Before
	public void setup() {
		Fixtures.delete(SupplierUser.class);
		Fixtures.delete(SupplierRole.class);
		Fixtures.loadModels("fixture/roles.yml");
		Fixtures.loadModels("fixture/cusers.yml");
	}


	@Test
	public void testCreate() {
		Map<String, String> params = new HashMap<String,String>();
		params.put("cuser.loginName", "yanjy");
		Images.Captcha captcha = Images.captcha();
		String password_salt =captcha.getText(6);
		params.put("cuser.encryptedPassword", DigestUtils.md5Hex("123456"+password_salt));
		params.put("cuser.companyId", "2");
		params.put("cuser.roles.id", "1");
		params.put("cuser.password_salt", password_salt);
		params.put("cuser.lockVersion","1");
		params.put("cuser.deleted",DeletedStatus.DELETED.toString());
		Response response = POST("/cusers", params);
		response.setContentTypeIfNotSet("text/html; charset=GBK");
		assertStatus(200,response);
		List<SupplierUser> list = SupplierUser.findAll();
		Assert.assertNotNull(list);  
	}

	/**
	 * 修改操作员信息
	 */
	@Test
	public void testEdit() {
		Long cuserId = (Long) Fixtures.idCache.get("models.admin" +
				".SupplierUser-SupplierUser_3");
		Long roleId = (Long) Fixtures.idCache.get("models.admin" +
				".SupplierRole-SupplierRole_2");
		Map<String, String> cuserParams = new HashMap<String,String>();
		cuserParams.put("cuser.loginName", "Hello");
		Images.Captcha captcha = Images.captcha();
		String password_salt =captcha.getText(6);
		cuserParams.put("cuser.encryptedPassword", DigestUtils.md5Hex("123456"+password_salt));
		cuserParams.put("cuser.companyId", "2");
		cuserParams.put("cuser.roles", String.valueOf(roleId));
		cuserParams.put("cuser.password_salt", password_salt);
		cuserParams.put("cuser.lockVersion","1");
		cuserParams.put("cuser.deleted",DeletedStatus.DELETED.toString());
		cuserParams.put("id", cuserId.toString());
		Http.Response response = POST("/cusers/"+cuserId, cuserParams);
		assertStatus(302,response);
		SupplierUser cuser = SupplierUser.findById(cuserId);
		Assert.assertNotNull(cuser);  
		assertEquals("Hello", cuser.loginName);
	}

	@Test
	public void testCheckLoginName() {
		Map<String, String> cuserParams = new HashMap<String,String>();
		cuserParams.put("loginName", "1");
		Http.Response response = POST("/cusers/checkLoginName", cuserParams);
		assertStatus(200,response);
	}

}
