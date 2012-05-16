package functional.models.accounts.util;

import models.accounts.Account;
import models.accounts.PaymentSource;
import models.accounts.TradeBill;
import models.accounts.util.TradeUtil;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.math.BigDecimal;

/**
 * @author likang
 * Date: 12-3-15
 */
public class TradeUtilTest extends FunctionalTest{
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
    public void testCreateOrderTrade(){
        Long id  = (Long)Fixtures.idCache.get("models.accounts.Account-account_1");
        Account account = Account.findById(id);
        assertNotNull(account);
        
        id = (Long)Fixtures.idCache.get("models.accounts.PaymentSource-alipay");
        PaymentSource aliPayment = PaymentSource.findById(id);
        assertNotNull(aliPayment);

        TradeBill tradeBill = TradeUtil.createOrderTrade(null,new BigDecimal(1), new BigDecimal(1), aliPayment,10L);
        assertNull(tradeBill);

        tradeBill = TradeUtil.createOrderTrade(account,new BigDecimal(-1), new BigDecimal(1), aliPayment,10L);
        assertNull(tradeBill);

        tradeBill = TradeUtil.createOrderTrade(account,new BigDecimal(1), new BigDecimal(-1), aliPayment,10L);
        assertNull(tradeBill);

        tradeBill = TradeUtil.createOrderTrade(account,new BigDecimal(1), new BigDecimal(1), null ,10L);
        assertNull(tradeBill);

        tradeBill = TradeUtil.createOrderTrade(account,new BigDecimal(1), new BigDecimal(1), aliPayment ,null);
        assertNull(tradeBill);


        tradeBill = TradeUtil.createOrderTrade(account,null, new BigDecimal(1), aliPayment ,10L);
        assertNull(tradeBill);

        tradeBill = TradeUtil.createOrderTrade(account,new BigDecimal(1), null, aliPayment ,10L);
        assertNull(tradeBill);


        tradeBill = TradeUtil.createOrderTrade(account,new BigDecimal(1), new BigDecimal(1), aliPayment ,10L);
        assertNotNull(tradeBill);
        assertNotNull(tradeBill.getId());
    }

    @Test
    public void testCreateChargeTrade(){
        Long id  = (Long)Fixtures.idCache.get("models.accounts.Account-account_1");
        Account account = Account.findById(id);
        assertNotNull(account);
        
        id = (Long)Fixtures.idCache.get("models.accounts.PaymentSource-alipay");
        PaymentSource aliPayment = PaymentSource.findById(id);
        assertNotNull(aliPayment);

        TradeBill tradeBill = TradeUtil.createChargeTrade(null,new BigDecimal(1), aliPayment, null);
        assertNull(tradeBill);
        
        tradeBill = TradeUtil.createChargeTrade(account,null, aliPayment, null);
        assertNull(tradeBill);

        tradeBill = TradeUtil.createChargeTrade(account, new BigDecimal(-1), aliPayment, null);
        assertNull(tradeBill);
        
        tradeBill = TradeUtil.createChargeTrade(account, new BigDecimal(0), aliPayment, null);
        assertNull(tradeBill);
        
        tradeBill = TradeUtil.createChargeTrade(account,new BigDecimal(1), null, null);
        assertNull(tradeBill);

        tradeBill = TradeUtil.createChargeTrade(account,new BigDecimal(1), aliPayment, null);
        assertNotNull(tradeBill);
        assertNotNull(tradeBill.getId());
        
    }

    @Test
    public void testCreateConsumeTrade(){
        Long id  = (Long)Fixtures.idCache.get("models.accounts.Account-account_1");
        Account account = Account.findById(id);
        assertNotNull(account);

        TradeBill tradeBill = TradeUtil.createConsumeTrade(null, account, new BigDecimal(1), null);
        assertNull(tradeBill);

        tradeBill = TradeUtil.createConsumeTrade("01234", null, new BigDecimal(1), null);
        assertNull(tradeBill);

        tradeBill = TradeUtil.createConsumeTrade("01234", account, new BigDecimal(-1), null);
        assertNull(tradeBill);

        tradeBill = TradeUtil.createConsumeTrade("01234", account, new BigDecimal(1), null);
        assertNotNull(tradeBill);
        assertNotNull(tradeBill.getId());
    }

    @Test
    public void testSuccess(){
        Long id  = (Long)Fixtures.idCache.get("models.accounts.Account-account_1");
        Account accountA = Account.findById(id);
        assertNotNull(accountA);
        id  = (Long)Fixtures.idCache.get("models.accounts.Account-account_2");
        Account accountB = Account.findById(id);
        assertNotNull(accountB);

        id = (Long)Fixtures.idCache.get("models.accounts.PaymentSource-alipay");
        PaymentSource aliPayment = PaymentSource.findById(id);
        assertNotNull(aliPayment);

        BigDecimal amountA = new BigDecimal("20.40");
        BigDecimal amountB = new BigDecimal("22.40");
        assertEquals(amountA, accountA.amount);
        assertEquals(amountB, accountB.amount);
    }

}
