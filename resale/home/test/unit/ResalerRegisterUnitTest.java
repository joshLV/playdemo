package unit;

import factory.FactoryBoy;
import models.resale.AccountType;
import models.resale.Resaler;
import models.resale.ResalerStatus;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import play.libs.Images;
import play.test.UnitTest;

import java.util.Date;
import java.util.List;

public class ResalerRegisterUnitTest extends UnitTest {
    Resaler resaler;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        resaler = FactoryBoy.create(Resaler.class);
    }

    @Test
    public void registersTest() {
        List<Resaler> list = Resaler.findAll();
        int cnt = list.size();
        assertEquals(1, list.size());
        Resaler resaler = new Resaler();
        resaler.loginName = "yyyy";
        resaler.mobile = "13000000001";
        Images.Captcha captcha = Images.captcha();
        String passwordSalt = captcha.getText(6);
        //密码加密
        resaler.password = DigestUtils.md5Hex("1" + passwordSalt);
        resaler.confirmPassword = "1";
        resaler.userName = "tom";
        //正常
        resaler.status = ResalerStatus.PENDING;
        //随机码
        resaler.passwordSalt = passwordSalt;
        resaler.address = "上海市";
        resaler.accountType = AccountType.COMPANY;
        resaler.email = "11@qq.com";
        resaler.loginIp = "127.0.0.1";
        resaler.lastLoginAt = new Date();
        resaler.createdAt = new Date();
        resaler.save();

        list = Resaler.findAll();
        assertEquals(cnt + 1, list.size());
    }

    //测试是否存在用户名和手机
    @Test
    public void testCheckValue() {
        String returnFlag = Resaler.checkValue("dangdang", "");
        assertEquals("1", returnFlag);

        returnFlag = Resaler.checkValue("dd", "13000000001");
        assertEquals("2", returnFlag);

        returnFlag = Resaler.checkValue("ee", "13213123125");
        assertEquals("0", returnFlag);
    }

}
