package functional.models.accounts.util;

import models.accounts.*;
import models.accounts.util.RefundUtil;
import models.consumer.User;
import models.order.Order;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Shop;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.math.BigDecimal;

/**
 * @author likang
 *         Date: 12-3-15
 */
public class RefundUtilTest extends FunctionalTest{
    @Before
    public void setup(){
        Fixtures.delete(TradeBill.class);

        Fixtures.delete(Account.class);
        Fixtures.delete(PaymentSource.class);


        Fixtures.delete(Order.class);
        Fixtures.delete(models.sales.Goods.class);
        Fixtures.delete(Category.class);
        Fixtures.delete(Shop.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(User.class);


        Fixtures.loadModels("fixture/accounts.yml");
        Fixtures.loadModels("fixture/account_uhuila.yml");
        Fixtures.loadModels("fixture/payment_source.yml");

        Fixtures.loadModels("fixture/categories_unit.yml");
        Fixtures.loadModels("fixture/shops_unit.yml");
        Fixtures.loadModels("fixture/brands_unit.yml");
        Fixtures.loadModels("fixture/user.yml");
        Fixtures.loadModels("fixture/goods_unit.yml");
        Fixtures.loadModels("fixture/orders.yml");
        Fixtures.loadModels("fixture/trade.yml");

    }

    @Test
    public void testRefundUtil(){
        Long id  = (Long)Fixtures.idCache.get("models.accounts.TradeBill-tradebill");
        TradeBill tradeBill = TradeBill.findById(id);
        assertNotNull(tradeBill);
        System.out.println(tradeBill.orderId);
        
        BigDecimal amount = new BigDecimal("10");
        RefundBill refundBill = RefundUtil.create(null, 10L, 10L, amount, "apply refund");
        assertNull(refundBill);

        tradeBill.tradeStatus = TradeStatus.UNPAID;
        refundBill = RefundUtil.create(tradeBill, 10L, 10L, amount, "apply refund");
        assertNull(refundBill);

        tradeBill.tradeStatus = TradeStatus.SUCCESS;
        refundBill = RefundUtil.create(tradeBill, 10L, 10L, amount, "apply refund");
        assertNotNull(refundBill);

        id  = (Long)Fixtures.idCache.get("models.accounts.Account-account_1");
        Account account = Account.findById(id);
        assertNotNull(account);

        BigDecimal origin_amount = account.amount;
        RefundUtil.success(refundBill);

        assertEquals(origin_amount.add(amount), account.amount);

        //重新加载
        account = Account.findById(id);
        assertNotNull(account);
        assertEquals(origin_amount.add(amount), account.amount);
    }
}
