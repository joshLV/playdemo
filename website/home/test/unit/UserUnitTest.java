package unit;

import models.accounts.Account;
import models.consumer.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.math.BigDecimal;

public class UserUnitTest extends UnitTest {
    @Before
    public void setup() {
        Fixtures.delete(User.class);
        Fixtures.delete(Account.class);
        Fixtures.loadModels("fixture/user.yml");
        Fixtures.loadModels("fixture/accounts.yml");
    }

    @Test
    public void passwordTest() {

        Long userId = (Long) Fixtures.idCache.get("models.consumer.User-selenium");
        User user = new User();
        user.password = "654321";
        User newUser = User.findById(userId);
        String password = newUser.password;

        user.updatePassword(newUser, user);
        User upduser = User.findById(userId);
        assertNotSame(password, upduser.password);
        assertEquals(DigestUtils.md5Hex("654321" + upduser.passwordSalt), upduser.password);
    }

    @Test
    public void AmountTest() {

        Long userId = (Long) Fixtures.idCache.get("models.consumer.User-selenium");
        Long id = (Long) Fixtures.idCache.get("models.accounts.Account-Account_2");
        Account account = Account.findById(id);
        account.uid = userId;
        account.save();


        User upduser = User.findById(userId);
        BigDecimal amount = upduser.AccountMoney();
        assertEquals(String.valueOf(1000.02), amount.toString());

        userId = (Long) Fixtures.idCache.get("models.consumer.User-selenium1");
        upduser = User.findById(userId);
        amount = upduser.AccountMoney();
        assertEquals(new BigDecimal(0), amount);
    }

}
