package function;


import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.accounts.*;
import models.admin.SupplierUser;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * User: wangjia
 * Date: 12-11-27
 * Time: 下午3:52
 */
public class SupplierWithdrawTest extends FunctionalTest {
    Account account;
    Supplier supplier;
    SupplierUser supplierUser;
    WithdrawBill withdrawBill;
    WithdrawAccount withdrawAccount;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        supplier = FactoryBoy.create(Supplier.class);
        account = FactoryBoy.create(Account.class, new BuildCallback<Account>() {
            @Override
            public void build(Account account) {
                account.accountType = AccountType.SUPPLIER;
                account.uid = supplier.id;
                account.amount = BigDecimal.valueOf(1000);
            }
        });
        supplierUser = FactoryBoy.create(SupplierUser.class);
        withdrawBill = FactoryBoy.create(WithdrawBill.class);


        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUser.loginName);
    }


    @Test
    public void testIndex() {
        Http.Response response = GET("/withdraw");
        assertIsOk(response);
        assertEquals(1, ((JPAExtPaginator<WithdrawBill>) renderArgs("billPage")).size());
    }


    @Test
    public void testApply() {
        Http.Response response = GET("/withdraw/apply");
        assertIsOk(response);
        assertEquals(account, (Account) renderArgs("account"));
    }


    @Test
    public void testDetail() {
        Http.Response response = GET("/withdraw/detail/" + withdrawBill.id);
        assertIsOk(response);
        assertEquals(withdrawBill, (WithdrawBill) renderArgs("bill"));
    }

    @Test
    public void testCreate() {
        withdrawAccount = FactoryBoy.create(WithdrawAccount.class, new BuildCallback<WithdrawAccount>() {
            @Override
            public void build(WithdrawAccount withdrawAccount) {
                withdrawAccount.userId = supplier.id;
                withdrawAccount.accountType = AccountType.SUPPLIER;
            }
        });
        assertEquals(1, WithdrawBill.count());
        Map<String, String> params = new HashMap<>();
        params.put("withdrawAccountId", withdrawAccount.id.toString());
        params.put("amount", BigDecimal.valueOf(20).toString());
        Http.Response response = POST("/withdraw/apply", params);
        assertStatus(302, response);
        assertEquals(2, WithdrawBill.count());
    }


}
