package unit.models.accounts;

import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.accounts.util.AccountUtil;
import models.operator.Operator;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Goods;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.math.BigDecimal;

/**
 * @author : likang
 */
public class OrderConsumeTest extends UnitTest {
    Goods realGoods;
    Goods eGoods;
    Order order;
    OrderItems orderItemElectric;
    OrderItems orderItemReal;
    ECoupon coupon;
    Account account;
    PaymentSource paymentSource;

    private static final BigDecimal BALANCE = new BigDecimal("100");

    private Account getAccount() {
        return AccountUtil.getAccount(999L, AccountType.CONSUMER);
    }

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        realGoods = FactoryBoy.create(Goods.class, "Real");
        order = FactoryBoy.create(Order.class, "orderForAccountsTest");
        orderItemReal = FactoryBoy.create(OrderItems.class, "orderItemReal");
        eGoods = FactoryBoy.create(Goods.class, "Electronic");
        orderItemElectric = FactoryBoy.create(OrderItems.class, "orderItemElectric");
        coupon = FactoryBoy.create(ECoupon.class, "couponForCommissionsTest");
        paymentSource = FactoryBoy.create(PaymentSource.class);

        account = getAccount();
        account.amount = BALANCE;
        order.setUser(account.uid, account.accountType);
        order.save();
    }

    /**
     * 测试余额+银行卡支付
     */
    @Test
    public void testConsume() {
        assertEquals(0, getAccount().amount.compareTo(BALANCE));

        Account account = order.chargeAccount();
        order.paid(account);
        assertEquals(0, BALANCE.subtract(order.accountPay).compareTo(getAccount().amount));//余额减少
        assertEquals(0, order.discountPay.negate().compareTo(AccountUtil.getPaymentPartnerAccount("alipay", Operator.defaultOperator()).amount));//支付宝虚拟账户
        assertEquals(0, order.accountPay.add(order.discountPay).compareTo(AccountUtil.getPlatformIncomingAccount(Operator.defaultOperator()).amount));

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
    public void testAllPayByBalance() {
        assertEquals(BALANCE, getAccount().amount);
        order.discountPay = BigDecimal.ZERO;
        order.save();

        Account account = order.chargeAccount();
        order.paid(account);
        assertEquals(0, BALANCE.subtract(order.accountPay).compareTo(getAccount().amount));//余额减少
        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getPaymentPartnerAccount("alipay", Operator.defaultOperator()).amount));//支付宝虚拟账户无变化
        assertEquals(0, order.accountPay.add(order.discountPay).compareTo(AccountUtil.getPlatformIncomingAccount(Operator.defaultOperator()).amount));

        //2个支付记录:
        //订单支付:账户余额减少
        //订单支付:系统收款账户余额增加
        assertEquals(2, AccountSequence.findAll().size());

    }

}
