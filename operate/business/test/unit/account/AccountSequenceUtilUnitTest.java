package unit.account;

import com.uhuila.common.util.DateUtil;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.util.AccountSequenceUtil;
import org.junit.Test;
import play.test.UnitTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 账户资金变动的工具类的单元测试.
 * <p/>
 * User: sujie
 * Date: 1/11/13
 * Time: 10:21 AM
 */
public class AccountSequenceUtilUnitTest extends UnitTest {
    int number = 0;

    @org.junit.Before
    public void setup() {
        FactoryBoy.deleteAll();
    }

    @Test
    public void testCheckBalance() {
        number = 0;
        FactoryBoy.batchCreate(10, AccountSequence.class, new SequenceCallback<AccountSequence>() {
            @Override
            public void sequence(AccountSequence target, int seq) {
                target.changeAmount = BigDecimal.TEN;
                target.balance = BigDecimal.TEN.add(target.changeAmount.multiply(new BigDecimal(number++)));
                target.cashBalance = target.balance;
            }
        });
        AccountSequence errSeq = AccountSequenceUtil.checkBalance(FactoryBoy.last(Account.class));
        assertNull(errSeq);
    }

    @Test
    public void testCheckAndFixAccountAmount() {
        number = 0;
        FactoryBoy.batchCreate(10, AccountSequence.class, new SequenceCallback<AccountSequence>() {
            @Override
            public void sequence(AccountSequence target, int seq) {
                target.changeAmount = BigDecimal.TEN;
                target.promotionChangeAmount = BigDecimal.TEN.add(BigDecimal.ONE);
                target.balance = BigDecimal.TEN.add(target.changeAmount.multiply(new BigDecimal(number++)));
                target.cashBalance = target.balance;
                target.promotionBalance = target.promotionChangeAmount.add(target.promotionChangeAmount.multiply(new BigDecimal(number - 1)));
            }
        });
        final Account account = FactoryBoy.last(Account.class);
        boolean fixed = AccountSequenceUtil.checkAndFixAccountAmount(account);
        assertTrue(fixed);
        assertEquals(100, account.amount.intValue());
        assertEquals(0, account.uncashAmount.intValue());
        assertEquals(110, account.promotionAmount.intValue());
    }

    @Test
    public void testCheckAndFixBalance() {
        number = 0;
        FactoryBoy.batchCreate(10, AccountSequence.class, new SequenceCallback<AccountSequence>() {
            @Override
            public void sequence(AccountSequence target, int seq) {
                target.changeAmount = BigDecimal.TEN;
                target.promotionChangeAmount = BigDecimal.TEN.add(BigDecimal.ONE);
                target.balance = BigDecimal.TEN;
                target.cashBalance = target.balance;
                target.promotionBalance = target.promotionChangeAmount;
            }
        });
        final Account account = FactoryBoy.last(Account.class);
        List<Account> accountList = new ArrayList<>();
        accountList.add(account);
        AccountSequenceUtil.checkAndFixBalance(accountList, DateUtil.getYesterday());
        AccountSequence lastAccountSequence = AccountSequence.getLastAccountSequence(account.id, null);

        assertEquals(100, lastAccountSequence.cashBalance.intValue());
        assertEquals(0, lastAccountSequence.uncashBalance.intValue());
        assertEquals(110, lastAccountSequence.promotionBalance.intValue());
    }
}
    