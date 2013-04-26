package unit.models.accounts;

import java.math.BigDecimal;

import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import factory.FactoryBoy;

/**
 * @author likang
 */
public class TransferTest extends UnitTest {
    private Account getAccount() {
        return AccountUtil.getCreditableAccount(999L, AccountType.CONSUMER);
    }

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
    }

    @Test
    public void transferTest() {
        BigDecimal amount = new BigDecimal("10");
        BigDecimal uncashAmount = new BigDecimal("20");

        assertEquals(0, BigDecimal.ZERO.compareTo(getAccount().amount));
        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getFinancingIncomingAccount().amount));

        TradeBill tradeBill = TradeUtil.transferTrade()
                .fromAccount(AccountUtil.getFinancingIncomingAccount())
                .toAccount(getAccount())
                .balancePaymentAmount(amount)
                .uncashPaymentAmount(uncashAmount)
                .make();
        TradeUtil.success(tradeBill, "测试转账");

        assertEquals(0, amount.compareTo(getAccount().amount));
        assertEquals(0, uncashAmount.compareTo(getAccount().uncashAmount));

        assertEquals(0, amount.negate().compareTo(AccountUtil.getFinancingIncomingAccount().amount));
        assertEquals(0, uncashAmount.negate().compareTo(AccountUtil.getFinancingIncomingAccount().uncashAmount));
    }
}
