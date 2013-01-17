package functional;

import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountType;
import models.consumer.User;
import models.resale.Resaler;
import models.supplier.Supplier;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;

import java.util.List;

/**
 * 运营后台简单核帐的功能测试.
 * <p/>
 * User: sujie
 * Date: 1/16/13
 * Time: 2:50 PM
 */
public class OperateFinancesTest extends FunctionalTest {
    Supplier supplier;
    Resaler resaler;
    User user;

    @org.junit.Before
    public void setup() {
        FactoryBoy.deleteAll();
        supplier = FactoryBoy.create(Supplier.class);
        Account account = FactoryBoy.create(Account.class);
        account.uid = supplier.id;
        account.accountType = AccountType.SUPPLIER;
        account.save();
        createAccountSequences();

        resaler = FactoryBoy.create(Resaler.class);
        account = FactoryBoy.create(Account.class);
        account.uid = resaler.id;
        account.accountType = AccountType.RESALER;
        account.save();
        createAccountSequences();

        user = FactoryBoy.create(User.class);
        account = FactoryBoy.create(Account.class);
        account.uid = user.id;
        account.accountType = AccountType.CONSUMER;
        account.save();
        createAccountSequences();
    }

    private void createAccountSequences() {
        AccountSequence seqFirst = FactoryBoy.create(AccountSequence.class);
        AccountSequence seqLast = FactoryBoy.create(AccountSequence.class);
        seqLast.cashBalance = seqFirst.cashBalance.add(seqFirst.changeAmount);
        seqLast.balance = seqLast.cashBalance.add(seqLast.uncashBalance);
        seqLast.save();
    }

    @Test
    public void testIndex() {
        Http.Response response = GET(Router.reverse("OperateFinancesTest.index").url);
        assertIsOk(response);

        List<Supplier> supplierList = (List<Supplier>) renderArgs("supplierList");
        assertEquals(1, supplierList.size());
    }

    @Test
    public void testCheckAccountSequence_SUPPLIER() {
        Http.Response response = GET(Router.reverse("OperateFinancesTest.checkAccountSequence").url
                + "?supplierId=" + supplier.id + "&accountType=SUPPLIER");
        assertIsOk(response);

        List<Supplier> supplierList = (List<Supplier>) renderArgs("supplierList");
        assertEquals(1, supplierList.size());
        boolean isOk = (Boolean) renderArgs("isOk");
        assertTrue(isOk);
        AccountSequence accountSequence = (AccountSequence) renderArgs("accountSequence");
        assertNull(accountSequence);
        Long supplierId = (Long) renderArgs("supplierId");
        assertEquals(supplier.id, supplierId);
        String resalerLoginName = (String) renderArgs("resalerLoginName");
        assertNull(resalerLoginName);
        String consumerLoginName = (String) renderArgs("consumerLoginName");
        assertNull(consumerLoginName);
        AccountType accountType = (AccountType) renderArgs("accountType");
        assertEquals(AccountType.SUPPLIER, accountType);
    }


    @Test
    public void testCheckAccountSequence_RESALER() {
        Http.Response response = GET(Router.reverse("OperateFinancesTest.checkAccountSequence").url
                + "?resalerLoginName=" + resaler.loginName + "&accountType=RESALER");
        assertIsOk(response);

        List<Supplier> supplierList = (List<Supplier>) renderArgs("supplierList");
        assertEquals(1, supplierList.size());
        boolean isOk = (Boolean) renderArgs("isOk");
        assertTrue(isOk);
        AccountSequence accountSequence = (AccountSequence) renderArgs("accountSequence");
        assertNull(accountSequence);
        Long supplierId = (Long) renderArgs("supplierId");
        assertNull(supplierId);
        String resalerLoginName = (String) renderArgs("resalerLoginName");
        assertEquals(resaler.loginName, resalerLoginName);
        String consumerLoginName = (String) renderArgs("consumerLoginName");
        assertNull(consumerLoginName);
        AccountType accountType = (AccountType) renderArgs("accountType");
        assertEquals(AccountType.RESALER, accountType);
    }


    @Test
    public void testCheckAccountSequence_CONSUMER() {
        Http.Response response = GET(Router.reverse("OperateFinancesTest.checkAccountSequence").url
                + "?consumerLoginName=" + user.loginName + "&accountType=CONSUMER");
        assertIsOk(response);

        List<Supplier> supplierList = (List<Supplier>) renderArgs("supplierList");
        assertEquals(1, supplierList.size());
        boolean isOk = (Boolean) renderArgs("isOk");
        assertTrue(isOk);
        AccountSequence accountSequence = (AccountSequence) renderArgs("accountSequence");
        assertNull(accountSequence);
        Long supplierId = (Long) renderArgs("supplierId");
        assertNull(supplierId);
        String resalerLoginName = (String) renderArgs("resalerLoginName");
        assertEquals(resaler.loginName, resalerLoginName);
        String consumerLoginName = (String) renderArgs("consumerLoginName");
        assertEquals(user.loginName, consumerLoginName);
        AccountType accountType = (AccountType) renderArgs("accountType");
        assertEquals(AccountType.CONSUMER, accountType);
    }

}
    