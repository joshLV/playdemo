package unit;

import models.consumer.User;
import models.consumer.UserInfo;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import factory.FactoryBoy;

public class UserInfosUnitTest extends UnitTest {
    User user;
    UserInfo userInfo;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        user = FactoryBoy.create(User.class);
        userInfo = FactoryBoy.create(UserInfo.class);
        userInfo.user = user;
        userInfo.save();
    }

    @Test
    public void testUpdateMobile() {
        UserInfo userInfo1 = new UserInfo();
        userInfo1.fullName = "小小";
        userInfo1.birthday = "2001-01-01";
        String interests = "1,2";
        userInfo.update(userInfo1, interests);
        assertEquals(userInfo1.fullName, userInfo.fullName);
        assertEquals(userInfo1.birthday, userInfo.birthday);
    }


    @Test
    public void testFindByUser() {
        assertNotNull(userInfo.findByUser(user));
    }

}
