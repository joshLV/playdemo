package unit;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.admin.SupplierRole;
import models.admin.SupplierUser;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import play.libs.Images;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.UnitTest;

import com.uhuila.common.constants.DeletedStatus;

public class CuserUnitTest extends UnitTest {

	@org.junit.Before
	public void setup() {
		Fixtures.delete(SupplierUser.class);
		Fixtures.delete(SupplierRole.class);
		Fixtures.loadModels("fixture/roles.yml");
		Fixtures.loadModels("fixture/cusers.yml");
	}

	//验证是否添加成功
	@Test
	public void testCreate(){
		List<SupplierUser> list = SupplierUser.findAll();
		int count = list.size();
		SupplierUser cuser = new SupplierUser();
		Images.Captcha captcha = Images.captcha();
		String password_salt =captcha.getText(6);
		//密码加密
		cuser.encryptedPassword = DigestUtils.md5Hex(cuser.encryptedPassword+password_salt);
		//随机吗
		cuser.passwordSalt = password_salt;
		cuser.lastLoginAt = new Date();
		cuser.createdAt = new Date();
		cuser.lockVersion = 0;
		cuser.supplierId = 1l;
		cuser.deleted = DeletedStatus.UN_DELETED;

		cuser.save();
		list = SupplierUser.findAll();
		assertNotNull(list);
		assertTrue(list.size() == count+1);
	}

	//测试是否查询到数据
	@Test
	public void testGetCuserList(){
		String loginName = "1";
		Long supplierId= 2l;
		int pageNumber=1;
		int pageSize=15;
		List<SupplierUser> list = SupplierUser.getCuserList(loginName,supplierId,pageNumber,pageSize);
		assertEquals(1,list.size());  

	}

	//更改用户名和手机
	@Test
	public void testUpdate(){
		Long id = (Long) Fixtures.idCache.get("models.admin.SupplierUser-SupplierUser_3");
		SupplierUser cuser = new SupplierUser();
		cuser.loginName="test";
		cuser.mobile="13899999999";
		SupplierUser.update(id, cuser);
		SupplierUser cusers = SupplierUser.findById(id);
		assertEquals(cusers.loginName,"test");  
		assertEquals(cusers.mobile,"13899999999");  
	}

	//测试是否存在用户名和手机
	@Test
	public void testCheckValue(){
		Long id = (Long) Fixtures.idCache.get("models.admin.SupplierUser-SupplierUser_3");
		String returnFlag = SupplierUser.checkValue(id,"2", "");
		assertEquals("1",returnFlag); 

		returnFlag = SupplierUser.checkValue(id,"808", "1300000000");
		assertEquals("2",returnFlag); 

		returnFlag = SupplierUser.checkValue(id,"808", "1300000003");
		assertEquals("0",returnFlag);
	}
}
