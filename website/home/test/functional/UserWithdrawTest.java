package functional;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.WithdrawBill;
import models.consumer.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户结算的功能测试.
 * <p/>
 * User: sujie
 * Date: 12/7/12
 * Time: 3:50 PM
 */
public class UserWithdrawTest extends FunctionalTest {
    User user;
    Account account;
    WithdrawBill withdrawBill;

    @Before
    public void setup() {
        FactoryBoy.lazyDelete();

        //设置虚拟登陆
        // 设置测试登录的用户名
        user = FactoryBoy.create(User.class);
        Security.setLoginUserForTest(user.loginName);
        account = FactoryBoy.create(Account.class);
        account.uid = user.id;
        account.accountType = models.accounts.AccountType.CONSUMER;
        account.amount = new java.math.BigDecimal("300.00");
        account.save();
        withdrawBill = FactoryBoy.create(WithdrawBill.class);

    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/withdraw");
        assertStatus(200, response);

        Account renderAccount = (Account) renderArgs("account");
        assertEquals(account.id, renderAccount.id);
        JPAExtPaginator<WithdrawBill> billPage = (JPAExtPaginator<WithdrawBill>) renderArgs("billPage");
        assertEquals(1, billPage.size());
        assertEquals(withdrawBill.userName, billPage.get(0).userName);
        assertEquals(withdrawBill.bankName, billPage.get(0).bankName);
    }

    @Test
    public void testGetApply() {
        Http.Response response = GET("/withdraw");
        assertStatus(200, response);
    }

    @Test
    public void testCreate() {
        Map<String, String> params = new HashMap<>();
        params.put("withdrawBill.userName", withdrawBill.userName);
        params.put("withdrawBill.subBankName", withdrawBill.subBankName);
        params.put("withdrawBill.cardNumber", "123");
        params.put("withdrawBill.amount", "200.00");
        Http.Response response = POST("/withdraw/apply", params);
        assertStatus(200, response);

        Account renderAccount = (Account) renderArgs("account");
        assertEquals(account.id, renderAccount.id);
    }


    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }
}
    