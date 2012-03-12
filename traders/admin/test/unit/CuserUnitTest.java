package unit;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;
import models.admin.SupplierRole;
import models.admin.SupplierUser;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import play.libs.Images;
import play.test.Fixtures;
import play.test.UnitTest;

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
		cuser.companyId = 1l;
		cuser.deleted = DeletedStatus.UN_DELETED;
		//获得本机IP
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			String ip = addr.getHostAddress();
			cuser.lastLoginIP = ip;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		cuser.save();
		list = SupplierUser.findAll();
		assertNotNull(list);
		Assert.assertTrue(list.size() == count+1);
	}

	//测试是否查询到数据
	@Test
	public void testGetCuserList(){
		String loginName = "1";
		Long companyId = 2l;
		int pageNumber=1;
		int pageSize=15;
		List<SupplierUser> list = SupplierUser.getCuserList(loginName,companyId,pageNumber,pageSize);
		Assert.assertEquals(1,list.size());  

	}
}
