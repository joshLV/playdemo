package functional.models.accounts.util;

import models.accounts.Account;
import models.accounts.ChargeBill;
import models.accounts.PaymentSource;
import models.accounts.util.ChargeUtil;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.math.BigDecimal;

/**
 * @author likang
 * Date: 12-3-15
 */
public class ChargeUtilTest extends FunctionalTest{
    @Before
    public void setup(){
        Fixtures.delete(Account.class);
        Fixtures.delete(PaymentSource.class);
//        Fixtures.delete(AccountSequence.class);
//        Fixtures.delete(CertificateDetail.class);
//        Fixtures.delete(SubjectDetail.class);
        Fixtures.loadModels("fixture/accounts.yml");
        Fixtures.loadModels("fixture/account_uhuila.yml");
        Fixtures.loadModels("fixture/payment_source.yml");
    }
    
    @Test
    public void testChargeUtil(){
        Long id  = (Long)Fixtures.idCache.get("models.accounts.Account-account_1");
        Account account = Account.findById(id);
        assertNotNull(account);

        id = (Long)Fixtures.idCache.get("models.accounts.PaymentSource-alipay");
        PaymentSource aliPayment = PaymentSource.findById(id);
        assertNotNull(aliPayment);

        BigDecimal amount = account.amount;
        BigDecimal add = new BigDecimal(1);
        
        ChargeBill chargeBill = ChargeUtil.create(account, add, aliPayment);
        assertNotNull(chargeBill);
        ChargeUtil.success(chargeBill);
        
        assertEquals(amount.add(add), account.amount);
    }
    
}
