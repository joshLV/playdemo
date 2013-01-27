package functional.payment;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.consumer.User;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.payment.PaymentUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;
import util.mq.MockMQ;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用testpay测试paymentnotify.
 */
public class PaymentNotifyTest extends FunctionalTest {

    Order order;
    Account account;
    User user;
    PaymentSource paymentSource;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        user = FactoryBoy.create(User.class);
        order = FactoryBoy.create(Order.class);
        FactoryBoy.create(OrderItems.class);
        order.setUser(user.id, AccountType.CONSUMER);
        order.needPay = BigDecimal.TEN;
        order.payMethod = "testpay";
        order.discountPay = BigDecimal.TEN;
        order.promotionBalancePay = BigDecimal.ZERO;
        order.status = OrderStatus.UNPAID;
        order.save();

        paymentSource = FactoryBoy.create(PaymentSource.class);
        paymentSource.code = "testpay";
        paymentSource.save();

        account = FactoryBoy.create(Account.class);
        account.amount = new java.math.BigDecimal("20.00");
        account.uid = user.id;
        account.save();

        Security.setLoginUserForTest(user.loginName);
    }


    @After
    public void tearDown() throws Exception {
        MockMQ.clear();
    }

    @Test
    public void testSuccessPayNotify() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("shihui_partner", PaymentUtil.PARTNER_CODE_TESTPAY);
        params.put("order_no", order.orderNumber);
        params.put("fee", order.needPay.toString());
        Http.Response response = POST(Router.reverse("PaymentNotify.notify").url, params);
        assertIsOk(response);
        assertEquals("success", getContent(response));
        order.refresh();
        assertEquals(OrderStatus.PAID, order.status);
    }

    @Test
    public void 重新通知已经支付成功的订单会继续成功_幂等() throws Exception {
        order.status = OrderStatus.PAID;
        order.save();
        Map<String, String> params = new HashMap<>();
        params.put("shihui_partner", PaymentUtil.PARTNER_CODE_TESTPAY);
        params.put("order_no", order.orderNumber);
        params.put("fee", order.needPay.toString());
        Http.Response response = POST(Router.reverse("PaymentNotify.notify").url, params);
        assertIsOk(response);
        assertEquals("success", getContent(response));
        order.refresh();
        assertEquals(OrderStatus.PAID, order.status);
    }

    @Test
    public void 返回金额不足时通知失败() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("shihui_partner", PaymentUtil.PARTNER_CODE_TESTPAY);
        params.put("order_no", order.orderNumber);
        params.put("fee", "9.0");
        Http.Response response = POST(Router.reverse("PaymentNotify.notify").url, params);
        assertIsOk(response);
        assertEquals("failed", getContent(response));
        order.refresh();
        assertEquals(OrderStatus.UNPAID, order.status);
    }
}
