package functional;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.consumer.User;
import models.order.Order;
import models.order.OrderECouponMessage;
import models.order.OrderItems;
import models.order.OrderStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import util.mq.MockMQ;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-11
 * Time: 上午11:38
 */
public class PaymentInfoTest extends FunctionalTest {
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
        order.payMethod = "balance";
        order.status = OrderStatus.UNPAID;
        order.consumerId=user.id;
        order.save();

        paymentSource = FactoryBoy.create(PaymentSource.class);
        paymentSource.code = "balance";
        paymentSource.save();

        account = FactoryBoy.create(Account.class);
        account.amount = new java.math.BigDecimal("20.00");
        account.uid = user.id;
        account.save();
        //设置虚拟登陆
        // 设置测试登录的用户名

        Security.setLoginUserForTest(user.loginName);
    }


    @After
    public void tearDown() throws Exception {
        MockMQ.clear();
    }


    @Test
    public void test_展示确认支付信息页() {
        Http.Response response = GET("/payment_info/" + order.orderNumber);
        assertStatus(200, response);
        Order order1 = (Order) renderArgs("order");
        Account account1 = (Account) renderArgs("account");
        assertEquals(order1, order);
        assertEquals(new BigDecimal("20.00"), account1.amount);
        assertContentMatch(" <div id=\"other-pay\" class=\"onlinepay\"style=\"display:block\">", response);
    }


    @Test
    public void test_全部用余额支付的订单() {
        Map<String, String> params = new HashMap<>();
        params.put("orderNumber", order.orderNumber);
        params.put("useBalance", "false");
        params.put("paymentSourceCode", "balance");
        Http.Response response = POST("/payment_info/confirm", params);
        assertStatus(200, response);
        Order order1 = (Order) renderArgs("order");
        PaymentSource paymentSource1 = (PaymentSource) renderArgs("paymentSource");
        assertEquals(order1, order);
        assertEquals(paymentSource.code, paymentSource1.code);
        assertContentMatch("你已选择 " + paymentSource1.name + " 支付", response);
    }

    @Test
    public void test_测试支付0元_网银支付不显示() {
        order.needPay = BigDecimal.ZERO;
        order.save();
        Http.Response response = GET("/payment_info/" + order.orderNumber);
        assertStatus(200, response);
        order.refresh();
        assertEquals(OrderStatus.PAID, order.status);
    }

    @Test
    public void test_测试支付0元() {
        order.needPay = BigDecimal.ZERO;
        order.save();
        Map<String, String> params = new HashMap<>();
        params.put("orderNumber", order.orderNumber);
        params.put("useBalance", "false");
        params.put("paymentSourceCode", "balance");
        Http.Response response = POST("/payment_info/confirm", params);
        assertStatus(200, response);
        Order order1 = (Order) renderArgs("order");
        PaymentSource paymentSource1 = (PaymentSource) renderArgs("paymentSource");
        assertEquals(order1.id, order.id);
        assertEquals(paymentSource.code, paymentSource1.code);
        assertContentMatch("恭喜您付款成功！", response);
        OrderECouponMessage message = (OrderECouponMessage) MockMQ.getLastMessage(OrderECouponMessage.MQ_KEY);
        assertNotNull(message);
    }

    @Test
    public void test_测试使用部分余额支付_网银支付显示() {
        order.needPay = new BigDecimal("50");
        order.save();
        Http.Response response = GET("/payment_info/" + order.orderNumber);
        assertStatus(200, response);
        assertContentMatch("用余额付款<em id=\"balance-pay\">20.00</em>元", response);
        assertContentMatch("剩余 <strong id=\"online-pay\">30.00</strong> 元可选择其他付款方式付款", response);
    }

    @Test
    public void test_测试使用部分余额支付() {
        order.needPay = new BigDecimal("50");
        paymentSource.code = "alipay";
        paymentSource.save();
        Map<String, String> params = new HashMap<>();
        params.put("orderNumber", order.orderNumber);
        params.put("useBalance", "true");
        params.put("paymentSourceCode", "alipay");
        Http.Response response = POST("/payment_info/confirm", params);
        assertStatus(200, response);
        System.out.println(response.out.toString());
        Order order1 = (Order) renderArgs("order");
        PaymentSource paymentSource1 = (PaymentSource) renderArgs("paymentSource");
        assertEquals(order1, order);
        assertEquals(paymentSource.code, paymentSource1.code);
        assertContentMatch("你已选择 zhifubao 支付 ", response);
    }

}
