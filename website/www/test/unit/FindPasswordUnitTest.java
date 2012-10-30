package unit;

import models.consumer.User;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import play.test.UnitTest;

public class FindPasswordUnitTest extends UnitTest {
    User user1;
    User user2;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        user1 = FactoryBoy.create(User.class, "user_test1");
        user2 = FactoryBoy.create(User.class, "user_test2");
    }

    @Test
    public void getUser() {
        String loginName = user1.loginName;
        boolean isExisted = user1.checkAndSendEmail(loginName);
        assertTrue(isExisted);
    }

    @Test
    public void checkMobile() {
        String mobile = user1.mobile;
        boolean returnFlag = user1.checkMobile(mobile);
        assertTrue(returnFlag);
    }

    @Test
    public void testUpdateFindPwd() {
        String totken = "63dc778349e8f64e7c1c3b9370848ba2";
        String password = "654321";
        user1.updateFindPwd(totken, "", "654321");
        user1 = User.findById(user1.id);
        assertEquals(DigestUtils.md5Hex(password + user1.passwordSalt), user1.password);

        String mobile = "15618096151";
        user1.updateFindPwd("", mobile, "654321");
        user1 = User.findById(user1.id);
        assertEquals(DigestUtils.md5Hex(password + user1.passwordSalt), user1.password);
    }

    @Test
    public void testIsExpired() {
        String totken = "63dc778349e8f64e7c1c3b9370848ba1";
        boolean isExpired = user2.isExpired(totken);
        assertTrue(isExpired);
    }


}
