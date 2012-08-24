package factory.consumer;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.consumer.User;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-8-23
 * Time: 上午10:06
 * To change this template use File | Settings | File Templates.
 */
public class UserFactory  extends ModelFactory<User> {

    @Override
    public User define() {
        User user = new User();
        user.loginName = "selenium@uhuila.com";
        user.password="selenium@uhuila.com";
        user.confirmPassword="selenium@uhuila.com";
        user.captcha="aa";
        return user;
    }

    @Factory(name = "random")
    public void defineRandomUser(User user) {
        user.loginName = "test" + FactoryBoy.sequence(User.class);
        user.password = "TestName" + FactoryBoy.sequence(User.class);
    }






}
