package factory.consumer;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.consumer.User;
import models.consumer.UserInfo;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-8-23
 * Time: 上午10:06
 * To change this template use File | Settings | File Templates.
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

        user.userInfo=FactoryBoy.last(UserInfo.class);
        return user;
    }

    @Factory(name = "random")
    public void defineRandomUser(User user) {
        user.loginName = "test" + FactoryBoy.sequence(User.class);
        user.password = "TestName" + FactoryBoy.sequence(User.class);
    }

    @Factory(name = "loginName")
    public User defineLoginNameUser(User user) {
        user.loginName = "11@qq.com";
        return user;
    }

}
