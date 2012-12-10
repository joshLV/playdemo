package functional;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountType;
import models.consumer.User;
import models.accounts.AccountSequence;
import models.accounts.util.AccountUtil;
import models.consumer.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.util.List;

/**
 * 用户资金的功能测试.
 *
 * @author likang
 * Date: 12-12-7
 */
public class UserSequencesTest extends FunctionalTest {
    AccountSequence sequence;
    @Before
    public void setUp() {
        FactoryBoy.deleteAll();

        // 设置虚拟登陆
        // 设置测试登录的用户名
        final User user = FactoryBoy.create(User.class);
        Security.setLoginUserForTest(user.loginName);

        FactoryBoy.create(Account.class, new BuildCallback<Account>() {
            @Override
            public void build(Account target) {
                target.uid = user.getId();
                target.accountType = AccountType.CONSUMER;
            }
        });

        sequence = FactoryBoy.create(AccountSequence.class);
        sequence.account = AccountUtil.getConsumerAccount(user.getId());

        sequence.save();
    }

    @Test
    public void testShow() {
        Http.Response response = GET("/user-sequences");
        assertIsOk(response);

        User user = FactoryBoy.last(User.class);
        assertEquals(user.id, ((User)renderArgs("user")).id);

        JPAExtPaginator<AccountSequence> amountList = (JPAExtPaginator<AccountSequence>)renderArgs("amountList");
        assertNotNull(amountList);
        assertEquals(1, amountList.size());
        assertEquals(sequence.serialNumber, amountList.get(0).serialNumber);
    }

}