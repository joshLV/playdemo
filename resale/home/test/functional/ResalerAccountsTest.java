package functional;

import controllers.ResalerAccounts;
import controllers.modules.resale.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountSequenceFlag;
import models.order.Order;
import models.order.OrderItems;
import models.resale.Resaler;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;

import java.util.Map;

/**
 * User: tanglq
 * Date: 13-1-10
 * Time: 下午4:37
 */
public class ResalerAccountsTest extends FunctionalTest {
    Resaler resaler;
    Account account;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        resaler = FactoryBoy.create(Resaler.class);
        Security.setLoginUserForTest(resaler.loginName);

        account = FactoryBoy.create(Account.class, "resaler");
        final Order order = FactoryBoy.create(Order.class);
        FactoryBoy.create(OrderItems.class);
        FactoryBoy.create(AccountSequence.class, new BuildCallback<AccountSequence>() {
            @Override
            public void build(AccountSequence target) {
                target.account = account;
                target.orderId = order.id;
            }
        });
    }

    @Test
    public void testIndex() throws Exception {
        Http.Response response = GET(Router.reverse("ResalerAccounts.index").url);
        assertIsOk(response);

        Map<AccountSequenceFlag, Object[]> summaryReport = (Map<AccountSequenceFlag, Object[]>) renderArgs("summaryReport");
        assertEquals(2, summaryReport.size());
    }

    @Test
    public void testInstance() throws Exception {
        assertTrue((new ResalerAccounts()) instanceof Controller);
    }
}
