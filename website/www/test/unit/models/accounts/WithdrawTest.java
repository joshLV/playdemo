package unit.models.accounts;

import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.TradeBill;
import models.accounts.WithdrawBill;
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
        account.amount = balance;
        account.save();
    }

    @Test
    public void testRefund(){

        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getPlatformWithdrawAccount().amount));
        assertEquals(0, balance.compareTo(getConsumerAccount().amount));
        assertEquals(0, BigDecimal.ZERO.compareTo(getConsumerAccount().uncashAmount));

        WithdrawBill bill = new WithdrawBill();
        bill.amount = balance;
        bill.save();
        bill.apply("测试提现者", getConsumerAccount());

        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getPlatformWithdrawAccount().amount));
        assertEquals(0, BigDecimal.ZERO.compareTo(getConsumerAccount().amount));
        assertEquals(0, balance.compareTo(getConsumerAccount().uncashAmount));

        bill.reject("测试拒绝");

        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getPlatformWithdrawAccount().amount));
        assertEquals(0, balance.compareTo(getConsumerAccount().amount));
        assertEquals(0, BigDecimal.ZERO.compareTo(getConsumerAccount().uncashAmount));


        bill.apply("测试提现者", getConsumerAccount());
        bill.agree(BigDecimal.ZERO, "测试提现成功");

        assertEquals(0, balance.compareTo(AccountUtil.getPlatformWithdrawAccount().uncashAmount));
        assertEquals(0, BigDecimal.ZERO.compareTo(getConsumerAccount().amount));
        assertEquals(0, BigDecimal.ZERO.compareTo(getConsumerAccount().uncashAmount));
    }
}
