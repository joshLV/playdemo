package unit;

import com.uhuila.common.constants.DeletedStatus;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import play.libs.Images;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.Date;
import java.util.List;

public class SupplierUserUnitTest extends UnitTest {

	@org.junit.Before
	public void setup() {
		Fixtures.delete(SupplierUser.class);
		Fixtures.delete(SupplierRole.class);
		Fixtures.loadModels("fixture/roles.yml");
		Fixtures.loadModels("fixture/supplier_users.yml");
	}

	//验证是否添加成功
	@Test
	public void testCreate(){
		List<SupplierUser> list = SupplierUser.findAll();
		int count = list.size();
		SupplierUser supplierUser = new SupplierUser();
		Images.Captcha captcha = Images.captcha();
		String password_salt =captcha.getText(6);
		//密码加密
		supplierUser.encryptedPassword = DigestUtils.md5Hex(supplierUser.encryptedPassword+password_salt);
		//随机吗
		supplierUser.passwordSalt = password_salt;
		supplierUser.lastLoginAt = new Date();
		supplierUser.createdAt = new Date();
		supplierUser.lockVersion = 0;
		supplierUser.deleted = DeletedStatus.UN_DELETED;

		supplierUser.save();
		list = SupplierUser.findAll();
		assertNotNull(list);
		assertTrue(list.size() == count+1);
	}

	//测试是否查询到数据
	@Test
	public void testGetCuserList(){
		String loginName = "1";
		Long supplierId= (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc");
		int pageNumber=1;
		int pageSize=15;
		List<SupplierUser> list = SupplierUser.getSupplierUserList(loginName,supplierId,pageNumber,pageSize);
		assertEquals(1,list.size()); 
	}

	//更改用户名和手机
	@Test
	public void testUpdate(){
		Long id = (Long) Fixtures.idCache.get("models.admin.SupplierUser-user3");
		SupplierUser supplierUser = new SupplierUser();
		supplierUser.loginName="test";
		supplierUser.mobile="13899999999";
		SupplierUser.update(id, supplierUser);
		SupplierUser user = SupplierUser.findById(id);
		assertEquals("test", user.loginName);
		assertEquals("13899999999", user.mobile);
	}

	//测试是否存在用户名和手机
	@Test
	public void testCheckValue(){
		Long id = (Long) Fixtures.idCache.get("models.admin.SupplierUser-user3");
        SupplierUser user = SupplierUser.findById(id);
        Long supplierUserId = user.supplier.id;
		String returnFlag = SupplierUser.checkValue(id,"2", "", supplierUserId);
		assertEquals("1",returnFlag); 

		returnFlag = SupplierUser.checkValue(id,"808", "1300000000", supplierUserId);
		assertEquals("2",returnFlag); 

		returnFlag = SupplierUser.checkValue(id,"808", "1300000003", supplierUserId);
		assertEquals("0",returnFlag);
	}
}
