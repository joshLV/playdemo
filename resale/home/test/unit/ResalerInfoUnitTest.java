package unit;

import factory.FactoryBoy;
import models.resale.Resaler;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

public class ResalerInfoUnitTest extends UnitTest {
    Resaler resaler;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        resaler = FactoryBoy.create(Resaler.class);
    }

    //测试是否存在用户名和手机
    @Test
    public void testUpdateInfo() {
        resaler.address = "徐家汇";
        resaler.mobile = "139555555555";
        resaler.userName = "xiao";
        Resaler.updateInfo(resaler.id, resaler);
        resaler.refresh();
        assertEquals("徐家汇", resaler.address);
        assertEquals("139555555555", resaler.mobile);
        assertEquals("xiao", resaler.userName);
    }

    @Test
    public void passwordTest() {
        resaler.password = "123456";
        Resaler newresaler = Resaler.findById(resaler.id);
        resaler.updatePassword(newresaler, resaler);
        resaler.refresh();
        assertEquals(DigestUtils.md5Hex("123456" + resaler.passwordSalt), resaler.password);
    }

}
