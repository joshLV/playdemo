package unit.models.accounts;

import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.AccountType;
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
        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getFinancingIncomingAccount(Operator.defaultOperator()).amount));

        TradeBill tradeBill = TradeUtil.transferTrade()
                .fromAccount(AccountUtil.getFinancingIncomingAccount(Operator.defaultOperator()))
                .toAccount(getAccount())
                .balancePaymentAmount(amount)
                .uncashPaymentAmount(uncashAmount)
                .make();
        TradeUtil.success(tradeBill, "测试转账");

        assertEquals(0, amount.compareTo(getAccount().amount));
        assertEquals(0, uncashAmount.compareTo(getAccount().uncashAmount));

        assertEquals(0, amount.negate().compareTo(AccountUtil.getFinancingIncomingAccount(Operator.defaultOperator()).amount));
        assertEquals(0, uncashAmount.negate().compareTo(AccountUtil.getFinancingIncomingAccount(Operator.defaultOperator()).uncashAmount));
    }
}
