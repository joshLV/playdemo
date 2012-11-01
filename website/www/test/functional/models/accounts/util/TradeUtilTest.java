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

import factory.FactoryBoy;

/**
 * @author likang
 *         Date: 12-3-15
 */
public class TradeUtilTest extends FunctionalTest {
    Account account;
    PaymentSource aliPayment;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        account = FactoryBoy.create(Account.class);
        aliPayment = FactoryBoy.create(PaymentSource.class);
    }

    @Test
    public void testCreateOrderTrade() {
        assertNotNull(account);

        assertNotNull(aliPayment);

        try {
            TradeUtil.createOrderTrade(null, new BigDecimal(1), new BigDecimal(1), BigDecimal.ZERO, BigDecimal.ZERO, aliPayment, 10L);
        } catch (IllegalArgumentException e) {
        }

        try {
            TradeUtil.createOrderTrade(account, new BigDecimal(-1), new BigDecimal(1), BigDecimal.ZERO, BigDecimal.ZERO, aliPayment, 10L);
        } catch (IllegalArgumentException e) {
        }

        try {
            TradeUtil.createOrderTrade(account, new BigDecimal(1), new BigDecimal(-1), BigDecimal.ZERO, BigDecimal.ZERO, aliPayment, 10L);
        } catch (IllegalArgumentException e) {
        }

        try {
            TradeUtil.createOrderTrade(account, new BigDecimal(1), new BigDecimal(1), BigDecimal.ZERO, BigDecimal.ZERO, null, 10L);
        } catch (IllegalArgumentException e) {
        }

        try {
            TradeUtil.createOrderTrade(account, new BigDecimal(1), new BigDecimal(1), BigDecimal.ZERO, BigDecimal.ZERO, aliPayment, null);
        } catch (IllegalArgumentException e) {
        }

        try {
            TradeUtil.createOrderTrade(account, null, new BigDecimal(1), BigDecimal.ZERO, BigDecimal.ZERO, aliPayment, 10L);
        } catch (IllegalArgumentException e) {
        }

        try {
            TradeUtil.createOrderTrade(account, new BigDecimal(1), null, BigDecimal.ZERO, BigDecimal.ZERO, aliPayment, 10L);
        } catch (IllegalArgumentException e) {
        }


        TradeBill tradeBill = TradeUtil.createOrderTrade(account, new BigDecimal(1), new BigDecimal(1), BigDecimal.ZERO, BigDecimal.ZERO, aliPayment, 10L);
        assertNotNull(tradeBill);
        assertNotNull(tradeBill.getId());
    }

    @Test
    public void testCreateChargeTrade() {
//        Long id = (Long) Fixtures.idCache.get("models.accounts.Account-account_1");
//        Account account = Account.findById(id);
        assertNotNull(account);

//        id = (Long) Fixtures.idCache.get("models.accounts.PaymentSource-alipay");
//        PaymentSource aliPayment = PaymentSource.findById(id);
        assertNotNull(aliPayment);

        try {
            TradeUtil.createChargeTrade(null, new BigDecimal(1), aliPayment, null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            TradeUtil.createChargeTrade(account, null, aliPayment, null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            TradeUtil.createChargeTrade(account, new BigDecimal(-1), aliPayment, null);
        } catch (IllegalArgumentException e) {
        }

        try {
            TradeUtil.createChargeTrade(account, new BigDecimal(0), aliPayment, null);
        } catch (IllegalArgumentException e) {
        }

        try {
            TradeUtil.createChargeTrade(account, new BigDecimal(1), null, null);
        } catch (IllegalArgumentException e) {
        }

        TradeBill tradeBill = TradeUtil.createChargeTrade(account, new BigDecimal(1), aliPayment, null);
        assertNotNull(tradeBill);
        assertNotNull(tradeBill.getId());

    }

    @Test
    public void testCreateConsumeTrade() {
//        Long id = (Long) Fixtures.idCache.get("models.accounts.Account-account_1");
//        Account account = Account.findById(id);
        assertNotNull(account);

        try {
            TradeUtil.createConsumeTrade(null, account, new BigDecimal(1), null);
        } catch (IllegalArgumentException e) {
        }

        try {
            TradeUtil.createConsumeTrade("01234", null, new BigDecimal(1), null);
        } catch (IllegalArgumentException e) {
        }

        try {
            TradeUtil.createConsumeTrade("01234", account, new BigDecimal(-1), null);
        } catch (IllegalArgumentException e) {
        }

        TradeBill tradeBill = TradeUtil.createConsumeTrade("01234", account, new BigDecimal(1), null);
        assertNotNull(tradeBill);
        assertNotNull(tradeBill.getId());
    }

}
