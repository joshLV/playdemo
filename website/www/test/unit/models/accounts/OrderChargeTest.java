package unit.models.accounts;

import java.math.BigDecimal;

import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.consumer.User;
import models.order.Order;
import models.order.OrderStatus;
import models.sales.Goods;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import factory.FactoryBoy;

/**
 * @author : likang
 */
public class OrderChargeTest extends UnitTest {

    private User user;
    private Goods goods;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        user = FactoryBoy.create(User.class);
        goods = FactoryBoy.create(Goods.class);
    }


    private Account getAccount() {
        return AccountUtil.getAccount(user.id, AccountType.CONSUMER);
    }

    /**
     * 测试充值
     */
    @Test
    public void testCharge() {
        Account account = getAccount();
        assertEquals(0, BigDecimal.ZERO.compareTo(account.amount));

        Order order = FactoryBoy.create(Order.class, "charge");
        Account accountOrder = order.chargeAccount();
        order.paid(accountOrder);

        assertEquals(OrderStatus.PAID, order.status);
        account = getAccount();
        assertEquals(0, order.discountPay.compareTo(account.amount));

        try {
            order.paid(accountOrder);
            fail("order can not be paid twice");
        } catch (RuntimeException e) {
        }
        account = getAccount();
        assertEquals(0, order.discountPay.compareTo(account.amount));
    }
}
