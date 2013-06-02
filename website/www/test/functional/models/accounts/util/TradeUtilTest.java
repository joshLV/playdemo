package functional.models.accounts.util;

import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.PaymentSource;
import models.accounts.TradeBill;
import models.accounts.util.TradeUtil;
import models.operator.Operator;
import org.junit.Before;
import org.junit.Test;
import play.test.FunctionalTest;

import java.math.BigDecimal;

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

        TradeBill tradeBill = TradeUtil.orderTrade(Operator.defaultOperator())
                .fromAccount(account)
                .balancePaymentAmount(BigDecimal.ONE)
                .ebankPaymentAmount(BigDecimal.ONE)
                .promotionPaymentAmount(BigDecimal.ZERO)
                .paymentSource(aliPayment)
                .orderId(10L)
                .make();
        assertNotNull(tradeBill);
        assertNotNull(tradeBill.getId());
    }

    @Test
    public void testCreateChargeTrade() {
        assertNotNull(account);

        assertNotNull(aliPayment);

        TradeBill tradeBill = TradeUtil.chargeTrade(aliPayment, Operator.defaultOperator())
                .ebankPaymentAmount(BigDecimal.ONE)
                .toAccount(account)
                .make();
        assertNotNull(tradeBill);
        assertNotNull(tradeBill.getId());

    }

    @Test
    public void testCreateConsumeTrade() {
        assertNotNull(account);

        TradeBill tradeBill = TradeUtil.consumeTrade(Operator.defaultOperator())
                .toAccount(account)
                .balancePaymentAmount(BigDecimal.ONE)
                .coupon("01234")
                .make();

        assertNotNull(tradeBill);
        assertNotNull(tradeBill.getId());
    }

}
