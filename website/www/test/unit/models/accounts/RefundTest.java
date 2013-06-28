package unit.models.accounts;

import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import models.operator.Operator;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.math.BigDecimal;

/**
 * @author likang
 */
public class RefundTest extends UnitTest {

    private Account getConsumerAccount() {
        return AccountUtil.getConsumerAccount(999L);
    }

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
    }

    @Test
    public void testRefund() {
        BigDecimal balance = new BigDecimal("1000");
        BigDecimal refundAmount = new BigDecimal("10");

        Account platformIncomingAccount = AccountUtil.getPlatformIncomingAccount(Operator.defaultOperator());
        platformIncomingAccount.amount = balance;
        platformIncomingAccount.save();

        assertEquals(0, balance.compareTo(AccountUtil.getPlatformIncomingAccount(Operator.defaultOperator()).amount));
        assertEquals(0, BigDecimal.ZERO.compareTo(getConsumerAccount().amount));

        TradeBill tradeBill = TradeUtil.refundFromPlatFormIncomingTrade(Operator.defaultOperator())
                .toAccount(getConsumerAccount())
                .balancePaymentAmount(refundAmount)
                .orderId(1L)
                .coupon("1")
                .make();
        TradeUtil.success(tradeBill, "退款");

        assertEquals(0, refundAmount.compareTo(getConsumerAccount().amount));
        assertEquals(0, AccountUtil.getPlatformIncomingAccount(Operator.defaultOperator()).amount.compareTo(balance.subtract(refundAmount)));
    }
}
