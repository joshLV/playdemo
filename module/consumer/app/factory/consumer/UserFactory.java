package factory.consumer;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.consumer.User;
import models.consumer.UserInfo;
import models.consumer.UserStatus;
import util.DateHelper;

import java.util.Date;

/**
 * User: wangjia
 * Date: 12-8-23
 * Time: 上午10:06
 */
public class UserFactory extends ModelFactory<User> {

    @Override
    public User define() {
        User user = new User();
        user.loginName = "selenium@uhuila.com";
        user.password = "selenium@uhuila.com";
        user.confirmPassword = "selenium@uhuila.com";
        user.captcha = "aa";
        user.mobile = "15026682165";
        user.promoterCode = "qweu2a";
        user.status = UserStatus.NORMAL;

        user.userInfo = FactoryBoy.lastOrCreate(UserInfo.class);
        return user;
    }

    @Factory(name = "random")
    public void defineRandomUser(User user) {
        user.loginName = "test" + FactoryBoy.sequence(User.class);
        user.password = "TestName" + FactoryBoy.sequence(User.class);
    }

    @Factory(name = "selenium")
    public User defineTomUser(User user) {
        user.loginName = "selenium@uhuila.com";
        user.password = "63dc778349e8f64e7c1c3b9370848ba1";
        user.passwordSalt = "gXP0W9";
        user.lastLoginAt = DateHelper.t("2012-02-24 15:40:07");
        user.status = UserStatus.NORMAL;
        user.mobile = "15618096151";
        return user;
    }

    @Factory(name = "loginName")
    public User defineLoginNameUser(User user) {
        user.loginName = "11@qq.com";
        user.promoterCode = "wr211a";
        user.password = "11@qq.com";
        user.mobile = "15026682168";
        return user;
    }

    @Factory(name = "user_test1")
    public User defineUserTest1(User user) {
        user.loginName = "selenium1@uhuila.com";
        user.password = "63dc778349e8f64e7c1c3b9370848ba1";
        user.passwordSalt = "gXP0W9";
        user.lastLoginAt = DateHelper.t("2012-02-24 15:40:07");
        user.status = UserStatus.NORMAL;
        user.mobile = "15618096151";
        user.passwordToken = "63dc778349e8f64e7c1c3b9370848ba2";
        user.sendMailAt = new Date();
        return user;
    }

    @Factory(name = "user_test2")
    public User defineUserTest2(User user) {
        user.loginName = "selenium2@uhuila.com";
        user.password = "63dc778349e8f64e7c1c3b9370848ba1";
        user.passwordSalt = "gXP0W9";
        user.lastLoginAt = DateHelper.t("2012-02-24 15:40:07");
        user.status = UserStatus.NORMAL;
        user.mobile = "15618096151";
        user.passwordToken = "63dc778349e8f64e7c1c3b9370848ba1";
        user.sendMailAt = DateHelper.t("2012-04-24T15:40:07");
        return user;
    }


}
