package unit;

import java.util.Date;
import java.util.List;

import models.consumer.User;
import models.resale.AccountType;
import models.resale.Resaler;
import models.resale.ResalerStatus;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import play.libs.Images;
import play.test.Fixtures;
import play.test.UnitTest;
public class RegisterUnitTest extends UnitTest {
	@Before
	public void setup() {
		Fixtures.delete(Resaler.class);
		Fixtures.loadModels("fixture/resaler.yml");
	}

	@Test
	public void passwordTest() { 
	
		Long resalerId = (Long) Fixtures.idCache.get("models.resale.Resaler-Resaler_1");
		Resaler resaler =new Resaler();
		resaler.password="123456";
		Resaler newresaler =Resaler.findById(resalerId);
		String password = newresaler.password;
		
		resaler.updatePassword(newresaler, resaler);
		Resaler updresaler =Resaler.findById(resalerId);
		assertNotSame(password, updresaler.password);  
		assertEquals( DigestUtils.md5Hex("123456"+updresaler.passwordSalt), updresaler.password);  
	}  
}
