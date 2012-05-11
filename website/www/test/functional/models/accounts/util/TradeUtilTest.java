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

        TradeBill tradeBill = TradeUtil.createChargeTrade(null,new BigDecimal(1), aliPayment);
        assertNull(tradeBill);
        
        tradeBill = TradeUtil.createChargeTrade(account,null, aliPayment);
        assertNull(tradeBill);

        tradeBill = TradeUtil.createChargeTrade(account, new BigDecimal(-1), aliPayment);
        assertNull(tradeBill);
        
        tradeBill = TradeUtil.createChargeTrade(account, new BigDecimal(0), aliPayment);
        assertNull(tradeBill);
        
        tradeBill = TradeUtil.createChargeTrade(account,new BigDecimal(1), null);
        assertNull(tradeBill);

        tradeBill = TradeUtil.createChargeTrade(account,new BigDecimal(1), aliPayment );
        assertNotNull(tradeBill);
        assertNotNull(tradeBill.getId());
        
    }

    @Test
    public void testCreateConsumeTrade(){
        Long id  = (Long)Fixtures.idCache.get("models.accounts.Account-account_1");
        Account account = Account.findById(id);
        assertNotNull(account);

        TradeBill tradeBill = TradeUtil.createConsumeTrade(null, account, new BigDecimal(1));
        assertNull(tradeBill);

        tradeBill = TradeUtil.createConsumeTrade("01234", null, new BigDecimal(1));
        assertNull(tradeBill);

        tradeBill = TradeUtil.createConsumeTrade("01234", account, new BigDecimal(-1));
        assertNull(tradeBill);

        tradeBill = TradeUtil.createConsumeTrade("01234", account, new BigDecimal(1));
        assertNotNull(tradeBill);
        assertNotNull(tradeBill.getId());
    }

    @Test
    public void testCreateTransferTrade(){
        Long id  = (Long)Fixtures.idCache.get("models.accounts.Account-account_1");
        Account accountA = Account.findById(id);
        assertNotNull(accountA);
        id  = (Long)Fixtures.idCache.get("models.accounts.Account-account_2");
        Account accountB = Account.findById(id);
        assertNotNull(accountB);

        id = (Long)Fixtures.idCache.get("models.accounts.PaymentSource-alipay");
        PaymentSource aliPayment = PaymentSource.findById(id);
        assertNotNull(aliPayment);

        TradeBill tradeBill = TradeUtil.createTransferTrade(null, accountB, new BigDecimal(1), new BigDecimal(1), aliPayment);
        assertNull(tradeBill);

        tradeBill = TradeUtil.createTransferTrade(accountA, null, null, new BigDecimal(1), aliPayment);
        assertNull(tradeBill);

        tradeBill = TradeUtil.createTransferTrade(accountA, accountB, new BigDecimal(-1), new BigDecimal(1), aliPayment);
        assertNull(tradeBill);

        tradeBill = TradeUtil.createTransferTrade(accountA, accountB, new BigDecimal(1), null, aliPayment);
        assertNull(tradeBill);

        tradeBill = TradeUtil.createTransferTrade(accountA, accountB, new BigDecimal(1), new BigDecimal(-1), aliPayment);
        assertNull(tradeBill);

        tradeBill = TradeUtil.createTransferTrade(accountA, accountB,new BigDecimal(1), new BigDecimal(1), null );
        assertNull(tradeBill);

        tradeBill = TradeUtil.createTransferTrade(accountA, accountB,new BigDecimal(1), new BigDecimal(1), aliPayment );
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
        
        BigDecimal accountPay = new BigDecimal("1");
        BigDecimal ebankPay = new BigDecimal("2");

        //验证正常转账
        System.out.println("==================");
        TradeBill tradeBill = TradeUtil.createTransferTrade(accountA, accountB,accountPay, ebankPay, aliPayment );
        assertNotNull(tradeBill);
        assertNotNull(tradeBill.getId());

        boolean result = TradeUtil.success(tradeBill);
        assertTrue(result);

        id  = (Long)Fixtures.idCache.get("models.accounts.Account-account_1");
        accountA = Account.findById(id);
        id  = (Long)Fixtures.idCache.get("models.accounts.Account-account_2");
        accountB = Account.findById(id);
        assertEquals(accountA.amount, amountA.subtract(accountPay).subtract(ebankPay));
        assertEquals(accountB.amount, amountB.add(ebankPay).add(accountPay));

        //重新从数据库加载
        id  = (Long)Fixtures.idCache.get("models.accounts.Account-account_1");
        accountA = Account.findById(id);
        id  = (Long)Fixtures.idCache.get("models.accounts.Account-account_2");
        accountB = Account.findById(id);

        assertEquals(accountA.amount, amountA.subtract(accountPay).subtract(ebankPay));
        assertEquals(accountB.amount, amountB.add(ebankPay).add(accountPay));

        //验证余额不足
        accountPay = new BigDecimal(100);
        amountA = accountA.amount;
        amountB = accountB.amount;

        tradeBill = TradeUtil.createTransferTrade(accountA, accountB, accountPay, ebankPay, aliPayment);
        assertNotNull(tradeBill);
        assertNotNull(tradeBill.getId());

        result = TradeUtil.success(tradeBill);
        assertFalse(result);

        assertEquals(accountA.amount, amountA);
        assertEquals(accountB.amount, amountB);
    }

}
