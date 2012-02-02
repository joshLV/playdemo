import org.apache.commons.codec.digest.DigestUtils;
import org.junit.*;

import common.CharacterUtil;

import java.util.*;
import play.test.*;
import models.*;

public class BasicTest extends UnitTest {

	@Before  
	public void setup() {  
		Fixtures.delete(Registers.class);  
	} 

	@Test
	public void aTest() {
		assertEquals(2, 1 + 1); // A really important thing to test
	}

	@Test
	public void registersTest() { 
		Registers r= new Registers();
		r.mobile=	"13000000000";
		r.email="admin@qq.com";
		String password_salt=CharacterUtil.getRandomString(6);
		//密码加密
		r.crypted_password=DigestUtils.md5Hex("123456"+password_salt);
		//正常
		r.status="1";

		//随机码
		r.password_salt=password_salt;

		r.login_ip="127.0.0.1";

		r.save();

		List<Registers> list = Registers.findAll(); 

		Assert.assertNotNull(list);  
		Assert.assertTrue(list.size() !=0);  
		Assert.assertEquals("admin@qq.com", list.get(0).email);  
	}  
}
