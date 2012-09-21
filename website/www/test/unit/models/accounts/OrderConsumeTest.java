package unit.models.accounts;

import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountType;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Goods;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.math.BigDecimal;

/**
 * @author : likang
 */
public class OrderConsumeTest extends UnitTest{
    private static final BigDecimal BALANCE = new BigDecimal("100");

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
        account.amount = BALANCE;
        Order order = getOrder();
        order.setUser(account.uid, account.accountType);
        order.save();
    }

    /**
     * 测试余额+银行卡支付
     */
    @Test
    public void testConsume(){
        assertEquals(0, getAccount().amount.compareTo(BALANCE));
        Order order = getOrder();
        order.paid();
        assertEquals(0, BALANCE.subtract(order.accountPay).compareTo(getAccount().amount));//余额减少
        assertEquals(0, order.discountPay.negate().compareTo(AccountUtil.getPaymentPartnerAccount("alipay").amount));//支付宝虚拟账户
        assertEquals(0, order.accountPay.add(order.discountPay).compareTo(AccountUtil.getPlatformIncomingAccount().amount));

        //4个支付记录:
        //账户充值:账户余额增加
        //账户充值:支付宝虚拟账户余额减少
        //订单支付:账户余额减少
        //订单支付:系统收款账户余额增加
        assertEquals(4, AccountSequence.findAll().size());
    }

    /**
     * 测试纯余额支付
     */
    public void testAllPayByBalance(){
        assertEquals(BALANCE ,getAccount().amount);
        Order order = getOrder();
        order.discountPay = BigDecimal.ZERO;
        order.save();

        getOrder().paid();
        assertEquals(0, BALANCE.subtract(order.accountPay).compareTo(getAccount().amount));//余额减少
        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getPaymentPartnerAccount("alipay").amount));//支付宝虚拟账户无变化
        assertEquals(0, order.accountPay.add(order.discountPay).compareTo(AccountUtil.getPlatformIncomingAccount().amount));

        //2个支付记录:
        //订单支付:账户余额减少
        //订单支付:系统收款账户余额增加
        assertEquals(2, AccountSequence.findAll().size());

    }

}
