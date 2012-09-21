package unit.models.accounts;

import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountType;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrderType;
import models.sales.Goods;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.math.BigDecimal;

/**
 * @author : likang
 */
public class OrderChargeTest extends UnitTest{

    private Account getAccount(){
        return AccountUtil.getAccount(999L, AccountType.CONSUMER);
    }

    private Order getOrder(){
        Long orderId = (Long)Fixtures.idCache.get("models.order.Order-simple_order");
        return Order.findById(orderId);
    }

    @Before
    public void setup(){
        Fixtures.delete(OrderItems.class);
        Fixtures.delete(Order.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(Account.class);
        Fixtures.delete(AccountSequence.class);
        Fixtures.delete(TradeBill.class);
        Fixtures.delete(ECoupon.class);
        Fixtures.loadModels("fixture/payment_source.yml", "fixture/account_test_order.yml");
        Account account = getAccount();
        Order order = getOrder();
        order.setUser(account.uid, account.accountType);
        order.accountPay = BigDecimal.ZERO;
        order.needPay = new BigDecimal("100");
        order.orderType= OrderType.CHARGE;
        order.save();
    }

    /**
     * 测试充值
     */
    @Test
    public void testCharge(){
        Account account = getAccount();
        assertEquals(0, BigDecimal.ZERO.compareTo(account.amount));
        Order order = getOrder();

        order.paid();
        account = getAccount();
        assertEquals(0, order.discountPay.compareTo(account.amount));

        try{
            order.paid();
            fail("order can not be paid twice");
        }catch (RuntimeException e){}
        account = getAccount();
        assertEquals(0, order.discountPay.compareTo(account.amount));
    }
}
