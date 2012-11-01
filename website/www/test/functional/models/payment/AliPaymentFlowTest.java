package functional.models.payment;

import models.accounts.Account;
import models.accounts.PaymentSource;
import models.consumer.User;
import models.order.Order;
import models.order.OrderStatus;
import models.payment.PaymentUtil;
import models.payment.PaymentFlow;
import models.sales.*;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.FunctionalTest;
import factory.FactoryBoy;

public class AliPaymentFlowTest extends FunctionalTest {
    User user;
    Category category;
    Area area;
    Brand brand;
    Shop shop;
    Goods goods;
    Account account;
    Order order;
    PaymentSource paymentSource;


    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        user = FactoryBoy.create(User.class);
        category = FactoryBoy.create(Category.class);
        area = FactoryBoy.create(Area.class);
        brand = FactoryBoy.create(Brand.class);
        shop = FactoryBoy.create(Shop.class);
        goods = FactoryBoy.create(Goods.class);
        order = FactoryBoy.create(Order.class, "orderForAccountsTest");
        account = FactoryBoy.create(Account.class);
        paymentSource = FactoryBoy.create(PaymentSource.class);
    }

    @Test
    public void testGenerateAliForm() {
        assertNotNull(order);
        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow("alipay");
        PaymentSource paymentSource = PaymentSource.findByCode(order.payMethod);
        assertNotNull(paymentSource);
        assertNotNull(paymentFlow.getRequestForm(order.orderNumber, order.description, order.discountPay, paymentSource.subPaymentCode, "127.0.0.1"));
    }

    @Test
    public void testGenerateTenpayForm() {
        assertNotNull(order);
        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow("tenpay");
        PaymentSource paymentSource = PaymentSource.findByCode(order.payMethod);
        assertNotNull(paymentSource);
        assertNotNull(paymentFlow.getRequestForm(order.orderNumber, order.description, order.discountPay, paymentSource.subPaymentCode, "127.0.0.1"));
    }

    @Test
    public void testGenerateKuaiqianForm() {
        assertNotNull(order);
        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow("99bill");
        PaymentSource paymentSource = PaymentSource.findByCode(order.payMethod);
        assertNotNull(paymentSource);
        assertNotNull(paymentFlow.getRequestForm(order.orderNumber, order.description, order.discountPay, paymentSource.subPaymentCode, "127.0.0.1"));
    }

}
