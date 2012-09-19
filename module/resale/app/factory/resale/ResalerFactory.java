package factory.resale;

import factory.ModelFactory;
import models.resale.AccountType;
import models.resale.Resaler;
import models.resale.ResalerLevel;
import models.resale.ResalerStatus;
import org.apache.commons.codec.digest.DigestUtils;
import play.libs.Images;

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
        Images.Captcha captcha = Images.captcha();
        String passwordSalt = captcha.getText(6);
        //密码加密
        resaler.password = DigestUtils.md5Hex("1" + passwordSalt);
        resaler.confirmPassword = "1";
        resaler.userName = "小李";
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
        resaler.save();
        return resaler;

    }

}
