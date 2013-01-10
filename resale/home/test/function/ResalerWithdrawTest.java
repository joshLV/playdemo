package function;

import controllers.modules.resale.cas.Security;
import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.WithdrawBill;
import models.resale.Resaler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User:yanjy
 * Time: 3:50 PM
 */
public class ResalerWithdrawTest extends FunctionalTest {
    Resaler user;
    Account account;
    WithdrawBill withdrawBill;

    @Before
    public void setup() {
        FactoryBoy.lazyDelete();

        //设置虚拟登陆
        // 设置测试登录的用户名
        user = FactoryBoy.create(Resaler.class);
        Security.setLoginUserForTest(user.loginName);
        account = FactoryBoy.create(Account.class);
        account.uid = user.id;
        account.accountType = AccountType.RESALER;
        account.amount = new java.math.BigDecimal("300.00");
        account.save();

        withdrawBill = FactoryBoy.create(WithdrawBill.class);
        withdrawBill.account = account;
        withdrawBill.save();
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/withdraw");
        assertStatus(200, response);

        List<WithdrawBill> withdrawBills = (List<WithdrawBill>) renderArgs("withdrawBills");
        assertEquals(1, withdrawBills.size());
        assertEquals(withdrawBill.userName, withdrawBills.get(0).userName);
        assertEquals(withdrawBill.bankName, withdrawBills.get(0).bankName);
    }

    @Test
    public void testGetApply() {
        Http.Response response = GET("/withdraw/apply");
        assertStatus(200, response);
        Account account1 = (Account) renderArgs("account");
        assertEquals(account1.uid, user.id.intValue());
    }

    @Test
    public void testCreate() {
        Map<String, String> params = new HashMap<>();
        params.put("withdraw.userName", withdrawBill.userName);
        params.put("withdraw.bankName", withdrawBill.subBankName);
        params.put("withdraw.subBankName", withdrawBill.subBankName);
        params.put("withdraw.cardNumber", "123");
        params.put("withdraw.bankCity", "sh");
        params.put("withdraw.amount", "200.00");
        Http.Response response = POST("/withdraw/apply", params);
        assertStatus(302, response);
        response = GET("/withdraw");
        assertContentMatch("等待审批", response);
    }


    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }
}
    