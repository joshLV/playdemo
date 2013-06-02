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
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.math.BigDecimal;

/**
 * @author likang
 */
public class CommissionsTest extends UnitTest {
    PaymentSource paymentSource;
    Goods realGoods;
    Goods eGoods;
    Order order;
    OrderItems orderItemElectric;
    OrderItems orderItemReal;
    Account account;
    ECoupon coupon;
    AccountSequence as;
    Supplier supplier;

    private static final BigDecimal BALANCE = new BigDecimal("1000000");

    private Account getConsumerAccount() {
        return AccountUtil.getAccount(999L, AccountType.CONSUMER);
    }

    private Order getOrder() {
        return Order.findById(order.id);
    }

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        supplier = FactoryBoy.create(Supplier.class);
        realGoods = FactoryBoy.create(Goods.class, "Real");
        order = FactoryBoy.create(Order.class, "orderForAccountsTest");
        orderItemReal = FactoryBoy.create(OrderItems.class, "orderItemReal");
        eGoods = FactoryBoy.create(Goods.class, "Electronic");
        orderItemElectric = FactoryBoy.create(OrderItems.class, "orderItemElectric");
        coupon = FactoryBoy.create(ECoupon.class, "couponForCommissionsTest");

        account = getConsumerAccount();

        order.setUser(account.uid, account.accountType);//普通用户购买
        order.save();


        //重置平台收款余额
        Account platformIncomingAccount = AccountUtil.getPlatformIncomingAccount(Operator.defaultOperator());
        platformIncomingAccount.amount = BALANCE;
        platformIncomingAccount.save();
    }


    @Test
    public void testRealGoodsCommission() {
        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getSupplierAccount(supplier.id, Operator.defaultOperator()).amount));//供应商余额为0
        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getUhuilaAccount(Operator.defaultOperator()).amount));//一百券余额为0
        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getPlatformCommissionAccount(Operator.defaultOperator()).amount)); //券平台佣金账户为0
        assertEquals(0, BALANCE.compareTo(AccountUtil.getPlatformIncomingAccount(Operator.defaultOperator()).amount));//平台收款账户

        assertNotNull(orderItemReal.id);
        OrderItems realOrderItem = OrderItems.findById(orderItemReal.id);
        assertNotNull(realOrderItem);
        order.refresh();

        Order.payRealGoodsCommissions(order.id);

        assertEquals(0,
                realOrderItem.originalPrice
                        .multiply(new BigDecimal(realOrderItem.buyNumber))
                        .compareTo(AccountUtil.getSupplierAccount(supplier.id, Operator.defaultOperator()).amount));//供应商余额

        assertEquals(0,
                realOrderItem.resalerPrice
                        .subtract(realOrderItem.originalPrice)
                        .multiply(new BigDecimal(realOrderItem.buyNumber))//佣金
                        .add(getOrder().freight)//加运费
                        .compareTo(AccountUtil.getPlatformCommissionAccount(Operator.defaultOperator()).amount));//平台佣金余额

        assertEquals(0,
                realOrderItem.salePrice
                        .subtract(realOrderItem.resalerPrice)
                        .multiply(new BigDecimal(realOrderItem.buyNumber))
                        .compareTo(AccountUtil.getUhuilaAccount(Operator.defaultOperator()).amount));//一百券佣金

        //8条账户变动记录:
        //商户收到成本价
        //一百券收到佣金
        //券佣金账户收到运费
        //券佣金账户收到佣金
        assertEquals(8, AccountSequence.findAll().size());
    }

    @Test
    public void testECouponCommission() {

        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getSupplierAccount(supplier.id, Operator.defaultOperator()).amount));//供应商余额为0
        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getUhuilaAccount(Operator.defaultOperator()).amount));//一百券余额为0
        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getPlatformCommissionAccount(Operator.defaultOperator()).amount)); //券平台佣金账户为0
        assertEquals(0, BALANCE.compareTo(AccountUtil.getPlatformIncomingAccount(Operator.defaultOperator()).amount));//平台收款账户

        assertNotNull(coupon.id);
        ECoupon eCoupon = ECoupon.findById(coupon.id);
        assertNotNull(eCoupon);

        eCoupon.payCommission();

        assertEquals(0,
                eCoupon.originalPrice
                        .compareTo(AccountUtil.getSupplierAccount(supplier.id, Operator.defaultOperator()).amount));//供应商余额

        assertEquals(0,
                eCoupon.resalerPrice
                        .subtract(eCoupon.originalPrice)
                        .compareTo(AccountUtil.getPlatformCommissionAccount(Operator.defaultOperator()).amount));//平台佣金余额

        assertEquals(0,
                eCoupon.salePrice
                        .subtract(eCoupon.resalerPrice)
                        .compareTo(AccountUtil.getUhuilaAccount(Operator.defaultOperator()).amount));//一百券佣金

        //6条账户变动记录:
        //商户收到成本价
        //一百券收到佣金
        //券佣金账户收到佣金
        assertEquals(6, AccountSequence.findAll().size());
    }
}
