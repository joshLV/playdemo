package functional.models.accounts.util;

import models.accounts.*;
import models.accounts.util.AccountUtil;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.math.BigDecimal;

/**
 * @author likang
 * Date: 12-3-7
 */
public class AccountUtilTest extends FunctionalTest{

    @Before
    public void setup(){
        Fixtures.delete(Account.class);
        Fixtures.delete(AccountSequence.class);
        Fixtures.delete(CertificateDetail.class);
        Fixtures.delete(SubjectDetail.class);
        Fixtures.loadModels("fixture/accounts.yml");
        Fixtures.loadModels("fixture/account_uhuila.yml");
    }

    @Test
    public void testGetUhuilaAccount(){
        Account account = AccountUtil.getUhuilaAccount();
        assertNotNull(account);
    }

    @Test
    public void testAddCash(){
        Long id  = (Long)Fixtures.idCache.get("models.accounts.Account-account_1");
        Account account = Account.findById(id);
        assertNotNull(account);

        Boolean exception = false;
        try{
            AccountUtil.addBalance(null, null, null, null, AccountSequenceType.REFUND, "test note");
        }catch (RuntimeException e){
            exception = true;
        }
        if(!exception){
            fail();
        }
        exception = false;
        try{
            AccountUtil.addBalance(null, null,null, new Long(1), null, "test note");
        }catch (RuntimeException e){
            exception = true;
        }
        if(!exception){
            fail();
        }
        BigDecimal originAmount = account.amount;

        BigDecimal augend = new BigDecimal(1);
        AccountUtil.addBalance(account, augend, null, new Long(1), AccountSequenceType.CHARGE, "test note");
        assertEquals(originAmount.add(augend), account.amount);

        assertEquals(1, AccountSequence.findAll().size());
        assertEquals(1, CertificateDetail.findAll().size());
        assertEquals(2, SubjectDetail.findAll().size());
        
    }
}
