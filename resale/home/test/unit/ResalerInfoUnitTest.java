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
public class ResalerInfoUnitTest extends UnitTest {
	@Before
	public void setup() {
		Fixtures.delete(Resaler.class);
		Fixtures.loadModels("fixture/resaler.yml");
	}

	//测试是否存在用户名和手机
	@Test
	public void testUpdateInfo(){
		
		Long resalerId = (Long) Fixtures.idCache.get("models.resale.Resaler-Resaler_2");
		Resaler resaler =new Resaler();
		resaler.address ="徐家汇";
		resaler.mobile = "139555555555";
		resaler.userName = "xiao";
		resaler.updateInfo(resalerId, resaler);
		Resaler updresaler =Resaler.findById(resalerId);
		assertEquals("徐家汇", updresaler.address);  
		assertEquals("139555555555", updresaler.mobile);  
		assertEquals("xiao", updresaler.userName);  
	}
	
}
