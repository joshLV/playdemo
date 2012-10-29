package unit;

import models.consumer.User;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import play.test.Fixtures;
import play.test.UnitTest;

public class FindPasswordUnitTest extends UnitTest {
    User user;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        user = FactoryBoy.create(User.class);
    }

    @Test
    public void getUser() {
        String loginName = user.loginName;
        boolean isExisted = user.checkAndSendEmail(loginName);
        assertTrue(isExisted);
    }

    @Test
    public void checkMobile() {
        String mobile = user.mobile;
        boolean returnFlag = user.checkMobile(mobile);
        assertTrue(returnFlag);
    }

    @Test
    public void testUpdateFindPwd() {
        String totken = "63dc778349e8f64e7c1c3b9370848ba2";
        String password = "654321";
        user.updateFindPwd(totken, "", "654321");
        user = User.findById(user.id);
        assertEquals(DigestUtils.md5Hex(password + user.passwordSalt), user.password);

        String mobile = "15618096151";
        user.updateFindPwd("", mobile, "654321");
        user = User.findById(user.id);
        assertEquals(DigestUtils.md5Hex(password + user.passwordSalt), user.password);
    }

    @Test
    public void testIsExpired() {
        String totken = "63dc778349e8f64e7c1c3b9370848ba1";
        boolean isExpired = user.isExpired(totken);
        assertTrue(isExpired);
    }


}
