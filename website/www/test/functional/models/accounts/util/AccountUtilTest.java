package functional.models.accounts.util;

import models.accounts.*;
import models.accounts.util.AccountUtil;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.math.BigDecimal;

import factory.FactoryBoy;


/**
 * @author likang
 *         Date: 12-3-7
 */
public class AccountUtilTest extends FunctionalTest {
    Account account;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        account = FactoryBoy.create(Account.class);
    }

    @Test
    public void testGetUhuilaAccount() {
        Account account = AccountUtil.getUhuilaAccount();
        assertNotNull(account);
    }

    @Test
    public void testAddCash() {
        assertNotNull(account);

        Boolean exception = false;
        try {
            AccountUtil.addBalanceAndSaveSequence(null, null, null, null, null, TradeType.REFUND, AccountSequenceFlag.VOSTRO, "test note", null);
        } catch (RuntimeException e) {
            exception = true;
        } catch (BalanceNotEnoughException e) {
            exception = true;
        } catch (AccountNotFoundException e) {
            exception = true;
        }
        if (!exception) {
            fail();
        }
        exception = false;
        try {
            AccountUtil.addBalanceAndSaveSequence(null, null, null, null, new Long(1), null, null, "test note", null);
        } catch (RuntimeException e) {
            exception = true;
        } catch (BalanceNotEnoughException e) {
            exception = true;
        } catch (AccountNotFoundException e) {
            exception = true;
        }
        if (!exception) {
            fail();
        }
        BigDecimal originAmount = account.amount;

        BigDecimal augend = new BigDecimal(1);
        try {
            AccountUtil.addBalanceAndSaveSequence(account.getId(), augend, BigDecimal.ZERO, BigDecimal.ZERO, new Long(1),
                    TradeType.CHARGE, AccountSequenceFlag.VOSTRO, "test note", null);
        } catch (BalanceNotEnoughException e) {

        } catch (AccountNotFoundException e) {
        }
        assertEquals(originAmount.add(augend), account.amount);

        assertEquals(1, AccountSequence.findAll().size());
    }
}
