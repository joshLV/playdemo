package unit.models.accounts;

import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.order.Order;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.math.BigDecimal;

/**
 * @author : likang
 */
public class OrderConsumeTest extends UnitTest{

    private Account getAccount(){
        return AccountUtil.getAccount(999L, AccountType.CONSUMER);
    }

    private Order getOrder(){
        Long orderId = (Long)Fixtures.idCache.get("models.order.Order-order_consume");
        return Order.findById(orderId);
    }

    @Before
    public void setup(){
        Fixtures.delete(Order.class);
        Fixtures.delete(Account.class);
        Fixtures.delete(AccountSequence.class);
        Fixtures.loadModels("fixture/payment_source.yml","fixture/order_consume.yml");
        Account account = getAccount();
        Order order = getOrder();
        order.setUser(account.uid, account.accountType);
        order.save();
    }

    /**
     * 测试支付
     */
    @Test
    public void testConsume(){
        Account account = getAccount();
        assertEquals(new BigDecimal("0"),account.amount);
        Order order = getOrder();
    }
}
