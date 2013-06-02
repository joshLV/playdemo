package unit.models.accounts;

import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.WithdrawBill;
import models.accounts.util.AccountUtil;
import models.operator.Operator;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author likang
 */
public class WithdrawTest extends UnitTest {
    BigDecimal balance = new BigDecimal("1000");

    private Account getConsumerAccount() {
        return AccountUtil.getConsumerAccount(999L);
    }

    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        Account account = getConsumerAccount();
        account.amount = balance;
        account.save();
    }

    @Test
    public void testRefund() {

        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getPlatformWithdrawAccount(Operator.defaultOperator()).amount));
        assertEquals(0, balance.compareTo(getConsumerAccount().amount));
        assertEquals(0, BigDecimal.ZERO.compareTo(getConsumerAccount().uncashAmount));

        WithdrawBill bill = new WithdrawBill();
        bill.amount = balance;
        bill.save();
        bill.apply("测试提现者", getConsumerAccount(), "consumerLoginName");

        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getPlatformWithdrawAccount(Operator.defaultOperator()).amount));
        assertEquals(0, BigDecimal.ZERO.compareTo(getConsumerAccount().amount));
        assertEquals(0, balance.compareTo(getConsumerAccount().uncashAmount));

        bill.reject("测试拒绝");

        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getPlatformWithdrawAccount(Operator.defaultOperator()).amount));
        assertEquals(0, balance.compareTo(getConsumerAccount().amount));
        assertEquals(0, BigDecimal.ZERO.compareTo(getConsumerAccount().uncashAmount));


        bill.apply("测试提现者", getConsumerAccount(), "consumerLoginName");
        bill.agree(BigDecimal.ZERO, "测试提现成功", new Date());

        assertEquals(0, balance.compareTo(AccountUtil.getPlatformWithdrawAccount(Operator.defaultOperator()).uncashAmount));
        assertEquals(0, BigDecimal.ZERO.compareTo(getConsumerAccount().amount));
        assertEquals(0, BigDecimal.ZERO.compareTo(getConsumerAccount().uncashAmount));
    }
}
