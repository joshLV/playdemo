package factory.resale;

import factory.ModelFactory;
import factory.annotation.Factory;
import models.resale.AccountType;
import models.resale.Resaler;
import models.resale.ResalerLevel;
import models.resale.ResalerStatus;
import org.apache.commons.codec.digest.DigestUtils;
import play.libs.Images;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-19
 * Time: 下午2:18
 */
public class ResalerFactory extends ModelFactory<Resaler> {

    @Override
    public Resaler define() {
        Resaler resaler = new Resaler();
        resaler.loginName = "dangdang";
        resaler.mobile = "13000000001";
        resaler.phone = "64986756";
        Images.Captcha captcha = Images.captcha();
        String passwordSalt = captcha.getText(6);
        //密码加密
        resaler.password = DigestUtils.md5Hex("1" + passwordSalt);
        resaler.confirmPassword = "1";
        resaler.userName = "小李";
        resaler.postCode = "200041";
        resaler.commissionRatio = BigDecimal.ZERO;

        //正常
        resaler.status = ResalerStatus.APPROVED;
        resaler.level = ResalerLevel.NORMAL;
        //随机码
        resaler.passwordSalt = passwordSalt;
        resaler.address = "上海市";
        resaler.accountType = AccountType.COMPANY;
        resaler.email = "11@qq.com";
        resaler.loginIp = "127.0.0.1";
        resaler.lastLoginAt = new Date();
        resaler.createdAt = new Date();
        resaler.password = "123456";
        resaler.confirmPassword = "123456";
        resaler.identityNo = "310106197812234089";
        resaler.save();
        return resaler;

    }

    @Factory(name = "jingdong")
    public Resaler defineWithJD(Resaler resaler) {
        resaler.loginName = "jingdong";
        return resaler;
    }

    @Factory(name = "wuba")
    public Resaler defineWuba(Resaler resaler) {
        resaler.loginName = "wuba";
        return resaler;
    }
}
