package functional.models.accounts.util;

import models.accounts.Account;
import models.accounts.PaymentSource;
import models.accounts.RefundBill;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.accounts.util.RefundUtil;
import models.accounts.util.TradeUtil;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.math.BigDecimal;

/**
 * @author likang
 *         Date: 12-3-15
 */
public class RefundUtilTest extends FunctionalTest{
    @Before
    public void setup(){
        Fixtures.delete(Account.class);
        Fixtures.delete(PaymentSource.class);

        Fixtures.delete(TradeBill.class);

        Fixtures.loadModels("fixture/accounts.yml");
        Fixtures.loadModels("fixture/account_uhuila.yml");
        Fixtures.loadModels("fixture/payment_source.yml");

    }

    @Test
    public void testRefundUtil(){
        Long id  = (Long)Fixtures.idCache.get("models.accounts.Account-account_1");
        Account accountA = Account.findById(id);
        assertNotNull(accountA);

        Account uhuilaAccount = AccountUtil.getUhuilaAccount();
        assertNotNull(uhuilaAccount);
        
        id = (Long)Fixtures.idCache.get("models.accounts.PaymentSource-alipay");
        PaymentSource aliPayment = PaymentSource.findById(id);
        assertNotNull(aliPayment);

    }
}
