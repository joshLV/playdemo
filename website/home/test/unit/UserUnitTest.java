package unit;

import factory.FactoryBoy;
import models.accounts.Account;
import models.consumer.OpenIdSource;
import models.consumer.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.util.Date;

public class UserUnitTest extends UnitTest {
    User user;
    Account account;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        user = FactoryBoy.create(User.class);
        account = FactoryBoy.create(Account.class);
    }

    @Test
    public void testUpdatePassword() {
        User newUser = User.findById(user.id);
        user.updatePassword(newUser, "654321");

        User updatedUser = User.findById(user.id);
        assertEquals(DigestUtils.md5Hex("654321" + updatedUser.passwordSalt), updatedUser.password);
    }

    @Test
    public void testCheckLoginName() {
        assertTrue(User.checkLoginName("selenium@uhuila.com"));
        assertFalse(User.checkLoginName("selenium"));
    }

    @Test
    public void testCheckMobile() {
        assertTrue(User.checkMobile("15026682165"));
        assertFalse(User.checkMobile("15026682000"));
    }

    @Test
    public void testGetUserByPromoterCode() {
        assertNotNull(User.getUserByPromoterCode("qweu2a"));
        assertNotNull(User.getUserByPromoterCode("QWEU2A"));

        assertNull(User.getUserByPromoterCode("qWeu2b"));
    }

    @Test
    public void testCheckOpenId() {
        assertFalse(User.checkOpenId(OpenIdSource.QQ, "12345678"));

        user.openIdSource = OpenIdSource.QQ;
        user.openId = "12345678";
        user.save();

        assertTrue(User.checkOpenId(OpenIdSource.QQ, "12345678"));
    }

    @Test
    public void testCheckAndSendEmail() {
        assertTrue(User.checkAndSendEmail("selenium@uhuila.com"));
    }

    @Test
    public void testFindByLoginName() {
        User foundUser = User.findByLoginName("selenium@uhuila.com");
        assertEquals(foundUser.id, user.id);
    }

    @Test
    public void testFindByLoginName_第三方帐号() {
        user.openIdSource = OpenIdSource.QQ;
        user.openId = "123456";
        user.save();

        User foundUser = User.findByLoginName(User.SOURCE_QQ + user.openId);
        assertEquals(user.id, foundUser.id);
    }

    @Test
    public void testCreate() {
        User user = new User();
        user.loginName = "selenium_create@uhuila.com";
        user.password = "selenium_create@uhuila.com";
        user.confirmPassword = "selenium_create@uhuila.com";
        user.captcha = "aa";
        user.mobile = "15026682166";
        user.promoterCode = "qweu2a";

        user.create();

        assertNotNull(User.findByLoginName("selenium_create@uhuila.com"));
    }

    @Test
    public void testUpdateMobile() throws InterruptedException {
        Date now = new Date();
        Thread.sleep(1000);
        assertNull(user.userInfo.bindMobileAt);

        user.updateMobile("13012341234");

        User updatedUser = User.findById(user.id);
        assertEquals(user.mobile, updatedUser.mobile);
//        assertTrue(user.userInfo.bindMobileAt.after(now));
    }

    @Test
    public void testGetAccount() {
        account.uid = user.id;
        account.save();

        Account userAccount = user.getAccount();
        assertEquals(account.id, userAccount.id);
    }

    @Test
    public void testIsOpenIdExpress() {
        assertTrue(User.isOpenIdExpress("QQ用户abc"));
        assertTrue(User.isOpenIdExpress("新浪微博用户abc"));
        assertTrue(User.isOpenIdExpress("人人网用户abc"));
        assertTrue(User.isOpenIdExpress("第三方用户abc"));
        assertTrue(User.isOpenIdExpress("用户abc..."));
    }


    @Test
    public void testGetOpenIdFromName() {
        assertEquals("abc", User.getOpenIdFromName("新浪微博用户abc"));
        assertEquals("abc", User.getOpenIdFromName("QQ用户abc..."));
        assertEquals("abc", User.getOpenIdFromName("人人网用户abc..."));
        assertEquals("abc", User.getOpenIdFromName("第三方用户abc..."));
        assertEquals("abc", User.getOpenIdFromName("用户abc..."));
    }

    @Test
    public void testGetOpenIdSourceFromName() {
        assertEquals(OpenIdSource.SinaWeibo, User.getOpenSourceFromName("新浪微博用户ab..."));
        assertEquals(OpenIdSource.RenRen, User.getOpenSourceFromName("人人网用户abc..."));
        assertEquals(OpenIdSource.QQ, User.getOpenSourceFromName("QQ用户abc..."));
        assertNull(User.getOpenSourceFromName("用户abc..."));
        assertNull(User.getOpenSourceFromName(null));
    }

}
