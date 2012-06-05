package functional.models.payment;

import models.accounts.PaymentSource;
import models.order.Order;
import models.payment.PaymentUtil;
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
    public void testGenerateAliForm(){
        Long id =  (Long)(Fixtures.idCache.get("models.order.Order-order_unpaid"));
        Order order = Order.findById(id);
        assertNotNull(order);
        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow("alipay");
        PaymentSource paymentSource = PaymentSource.findByCode(order.payMethod);
        assertNotNull(paymentSource);
        assertNotNull(paymentFlow.getRequestForm(order.orderNumber,order.description,order.discountPay,paymentSource.subPaymentCode,"127.0.0.1"));
    }

    @Test
    public void testGenerateTenpayForm(){
        Long id =  (Long)(Fixtures.idCache.get("models.order.Order-order_unpaid"));
        Order order = Order.findById(id);
        assertNotNull(order);
        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow("tenpay");
        PaymentSource paymentSource = PaymentSource.findByCode(order.payMethod);
        assertNotNull(paymentSource);
        assertNotNull(paymentFlow.getRequestForm(order.orderNumber, order.description, order.discountPay, paymentSource.subPaymentCode, "127.0.0.1"));
    }

    @Test
    public void testGenerateKuaiqianForm(){
        Long id =  (Long)(Fixtures.idCache.get("models.order.Order-order_unpaid"));
        Order order = Order.findById(id);
        assertNotNull(order);
        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow("99bill");
        PaymentSource paymentSource = PaymentSource.findByCode(order.payMethod);
        assertNotNull(paymentSource);
        assertNotNull(paymentFlow.getRequestForm(order.orderNumber, order.description, order.discountPay, paymentSource.subPaymentCode, "127.0.0.1"));
    }

}
