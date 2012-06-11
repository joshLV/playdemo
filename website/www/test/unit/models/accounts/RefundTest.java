package unit.models.accounts;

import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.math.BigDecimal;

/**
 * @author likang
 */
public class RefundTest extends UnitTest{

    private Account getConsumerAccount(){
        return AccountUtil.getConsumerAccount(999L);
    }

    @Before
    public void setup(){
        Fixtures.delete(Account.class);
        Fixtures.delete(AccountSequence.class);
        Fixtures.delete(TradeBill.class);
    }

    @Test
    public void testRefund(){
        BigDecimal balance = new BigDecimal("1000");
        BigDecimal refundAmount = new BigDecimal("10");

        Account platformIncomingAccount = AccountUtil.getPlatformIncomingAccount();
        platformIncomingAccount.amount = balance;
        platformIncomingAccount.save();

        assertEquals(0, balance.compareTo(AccountUtil.getPlatformIncomingAccount().amount));
        assertEquals(0, BigDecimal.ZERO.compareTo(getConsumerAccount().amount));

        TradeBill tradeBill = TradeUtil.createRefundTrade(getConsumerAccount(), refundAmount, 1L);
        TradeUtil.success(tradeBill, "退款");

        assertEquals(0, refundAmount.compareTo(getConsumerAccount().amount));
        assertEquals(0, AccountUtil.getPlatformIncomingAccount().amount.compareTo(balance.subtract(refundAmount)));
    }
}
