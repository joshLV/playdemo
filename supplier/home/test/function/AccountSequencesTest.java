package function;

import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountType;
import models.admin.SupplierUser;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.math.BigDecimal;

/**
 * User: wangjia
 * Date: 12-11-27
 * Time: 下午5:40
 */
public class AccountSequencesTest extends FunctionalTest {
    Account account;
    Supplier supplier;
    SupplierUser supplierUser;
    AccountSequence accountSequence;

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
        accountSequence = FactoryBoy.create(AccountSequence.class);

        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUser.loginName);

    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/account-sequences");
        assertIsOk(response);
        assertEquals(1, ((JPAExtPaginator<AccountSequence>) renderArgs("accountSequences")).size());

    }


}
