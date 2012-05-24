package functional.models.payment;

import models.order.Order;
import models.payment.alipay.AliPaymentFlow;
import models.payment.PaymentFlow;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.FunctionalTest;

public class AliPaymentFlowTest extends FunctionalTest{
	
	@Before
    public void setup(){
        Fixtures.delete(models.order.Order.class);
        Fixtures.delete(models.sales.Category.class);
        Fixtures.delete(models.sales.Area.class);
        Fixtures.delete(models.sales.Shop.class);
        Fixtures.delete(models.sales.Goods.class);
        Fixtures.delete(models.order.OrderItems.class);
        Fixtures.delete(models.order.ECoupon.class);
        Fixtures.delete(models.accounts.Account.class);

        Fixtures.loadModels("fixture/categories_unit.yml");
        Fixtures.loadModels("fixture/areas_unit.yml");
        Fixtures.loadModels("fixture/brands_unit.yml");
        Fixtures.loadModels("fixture/user.yml");
        Fixtures.loadModels("fixture/shops_unit.yml");
        Fixtures.loadModels("fixture/goods_unit.yml");
        Fixtures.loadModels("fixture/orders.yml");
        Fixtures.loadModels("fixture/accounts.yml");
        Fixtures.loadModels("fixture/account_uhuila.yml");
        Fixtures.loadModels("fixture/payment_source.yml");
    }

    @Test
    public void testGenerageForm(){
        Long id = (long) Fixtures.idCache.get("models.order.Order-order_unpaid");
        Order order = Order.findById(id);
        assertNotNull(order);
        PaymentFlow paymentFlow = new AliPaymentFlow();
        assertNotNull(paymentFlow.generateForm(order));
    }

}
