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
 * @author likang
 */
public class CommissionsTest extends UnitTest{
    private static final BigDecimal BALANCE = new BigDecimal("1000000");

    private Account getConsumerAccount(){
        return AccountUtil.getAccount(999L, AccountType.CONSUMER);
    }

    private Order getOrder(){
        Long orderId = (Long) Fixtures.idCache.get("models.order.Order-simple_order");
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
        Fixtures.loadModels("fixture/payment_source.yml", "fixture/account_test_order.yml");

        Account account = getConsumerAccount();
        Order order = getOrder();
        order.setUser(account.uid, account.accountType);//普通用户购买
        order.save();

        //重置平台收款余额
        Account platformIncomingAccount = AccountUtil.getPlatformIncomingAccount();
        platformIncomingAccount.amount = BALANCE;
        platformIncomingAccount.save();
    }

    @Test
    public void testRealGoodsCommission(){
        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getSupplierAccount(1L).amount));//供应商余额为0
        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getUhuilaAccount().amount));//一百券余额为0
        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getPlatformCommissionAccount().amount)); //券平台佣金账户为0
        assertEquals(0, BALANCE.compareTo(AccountUtil.getPlatformIncomingAccount().amount));//平台收款账户


        Long realOrderItemId = (Long) Fixtures.idCache.get("models.order.OrderItems-order_item_real");
        assertNotNull(realOrderItemId);
        OrderItems realOrderItem = OrderItems.findById(realOrderItemId);
        assertNotNull(realOrderItem);

        Order.payRealGoodsCommissions(getOrder().getId());

        assertEquals(0,
                realOrderItem.originalPrice
                .multiply(new BigDecimal(realOrderItem.buyNumber))
                .compareTo(AccountUtil.getSupplierAccount(1L).amount));//供应商余额

        assertEquals(0,
                realOrderItem.resalerPrice
                .subtract(realOrderItem.originalPrice)
                .multiply(new BigDecimal(realOrderItem.buyNumber))//佣金
                .add(getOrder().freight)//加运费
                .compareTo(AccountUtil.getPlatformCommissionAccount().amount));//平台佣金余额

        assertEquals(0,
                realOrderItem.salePrice
                .subtract(realOrderItem.resalerPrice)
                .multiply(new BigDecimal(realOrderItem.buyNumber))
                .compareTo(AccountUtil.getUhuilaAccount().amount));//一百券佣金

        //8条账户变动记录:
        //商户收到成本价
        //一百券收到佣金
        //券佣金账户收到运费
        //券佣金账户收到佣金
        assertEquals(8, AccountSequence.findAll().size());
    }

    @Test
    public void testECouponCommission(){

        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getSupplierAccount(1L).amount));//供应商余额为0
        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getUhuilaAccount().amount));//一百券余额为0
        assertEquals(0, BigDecimal.ZERO.compareTo(AccountUtil.getPlatformCommissionAccount().amount)); //券平台佣金账户为0
        assertEquals(0, BALANCE.compareTo(AccountUtil.getPlatformIncomingAccount().amount));//平台收款账户


        Long eCouponId = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon");
        assertNotNull(eCouponId);
        ECoupon eCoupon = ECoupon.findById(eCouponId);
        assertNotNull(eCoupon);

        eCoupon.payCommission();

        assertEquals(0,
                eCoupon.originalPrice
                        .compareTo(AccountUtil.getSupplierAccount(1L).amount));//供应商余额

        assertEquals(0,
                eCoupon.resalerPrice
                        .subtract(eCoupon.originalPrice)
                        .compareTo(AccountUtil.getPlatformCommissionAccount().amount));//平台佣金余额

        assertEquals(0,
                eCoupon.salePrice
                        .subtract(eCoupon.resalerPrice)
                        .compareTo(AccountUtil.getUhuilaAccount().amount));//一百券佣金

        //6条账户变动记录:
        //商户收到成本价
        //一百券收到佣金
        //券佣金账户收到佣金
        assertEquals(6, AccountSequence.findAll().size());
    }
}
