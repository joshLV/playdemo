package functional.models.accounts.util;

import java.math.BigDecimal;

import models.accounts.Account;
import models.accounts.PaymentSource;
import models.accounts.TradeBill;
import models.accounts.util.TradeUtil;

import org.junit.Before;
import org.junit.Test;

import play.test.FunctionalTest;
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
            TradeUtil.createOrderTrade(null, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, aliPayment, 10L);
        } catch (IllegalArgumentException e) {
        }

        try {
            TradeUtil.createOrderTrade(account, new BigDecimal(-1), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, aliPayment, 10L);
        } catch (IllegalArgumentException e) {
        }

        try {
            TradeUtil.createOrderTrade(account, BigDecimal.ONE, new BigDecimal(-1), BigDecimal.ZERO, BigDecimal.ZERO, aliPayment, 10L);
        } catch (IllegalArgumentException e) {
        }

        try {
            TradeUtil.createOrderTrade(account, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, null, 10L);
        } catch (IllegalArgumentException e) {
        }

        try {
            TradeUtil.createOrderTrade(account, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, aliPayment, null);
        } catch (IllegalArgumentException e) {
        }

        try {
            TradeUtil.createOrderTrade(account, null, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, aliPayment, 10L);
        } catch (IllegalArgumentException e) {
        }

        try {
            TradeUtil.createOrderTrade(account, BigDecimal.ONE, null, BigDecimal.ZERO, BigDecimal.ZERO, aliPayment, 10L);
        } catch (IllegalArgumentException e) {
        }


        TradeBill tradeBill = TradeUtil.createOrderTrade(account, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, aliPayment, 10L);
        assertNotNull(tradeBill);
        assertNotNull(tradeBill.getId());
    }

    @Test
    public void testCreateChargeTrade() {
//        Account account = Account.findById(id);
        assertNotNull(account);

//        PaymentSource aliPayment = PaymentSource.findById(id);
        assertNotNull(aliPayment);

        try {
            TradeUtil.createChargeTrade(null, BigDecimal.ONE, aliPayment, null);
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
            TradeUtil.createChargeTrade(account, BigDecimal.ZERO, aliPayment, null);
        } catch (IllegalArgumentException e) {
        }

        try {
            TradeUtil.createChargeTrade(account, BigDecimal.ONE, null, null);
        } catch (IllegalArgumentException e) {
        }

        TradeBill tradeBill = TradeUtil.createChargeTrade(account, BigDecimal.ONE, aliPayment, null);
        assertNotNull(tradeBill);
        assertNotNull(tradeBill.getId());

    }

    @Test
    public void testCreateConsumeTrade() {
//        Account account = Account.findById(id);
        assertNotNull(account);

        try {
            TradeUtil.createConsumeTrade(null, account, BigDecimal.ONE, null);
        } catch (IllegalArgumentException e) {
        }

        try {
            TradeUtil.createConsumeTrade("01234", null, BigDecimal.ONE, null);
        } catch (IllegalArgumentException e) {
        }

        try {
            TradeUtil.createConsumeTrade("01234", account, new BigDecimal(-1), null);
        } catch (IllegalArgumentException e) {
        }

        TradeBill tradeBill = TradeUtil.createConsumeTrade("01234", account, BigDecimal.ONE, null);
        assertNotNull(tradeBill);
        assertNotNull(tradeBill.getId());
    }

}
