package unit.models.accounts;

import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.math.BigDecimal;

/**
 * @author likang
 */
public class WithdrawTest extends UnitTest{
    BigDecimal balance = new BigDecimal("1000");

    private Account getConsumerAccount(){
        return AccountUtil.getConsumerAccount(999L);
    }

    @Before
    public void setup(){
        Fixtures.delete(Account.class);
        Fixtures.delete(AccountSequence.class);
        Fixtures.delete(TradeBill.class);

        Account account = getConsumerAccount();
        account.uncashAmount = balance;
        account.save();
    }

    @Test
    public void testRefund(){

        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getPlatformWithdrawAccount().amount));
        assertEquals(0, BigDecimal.ZERO.compareTo(getConsumerAccount().amount));
        assertEquals(0, balance.compareTo(getConsumerAccount().uncashAmount));

        TradeBill tradeBill = TradeUtil.createWithdrawTrade(getConsumerAccount(), balance);
        TradeUtil.success(tradeBill, "提现");

        assertEquals(0, balance.compareTo(AccountUtil.getPlatformWithdrawAccount().uncashAmount));
        assertEquals(0, BigDecimal.ZERO.compareTo(getConsumerAccount().amount));
        assertEquals(0, BigDecimal.ZERO.compareTo(getConsumerAccount().uncashAmount));
    }
}
