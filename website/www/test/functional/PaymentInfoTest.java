package functional;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.consumer.User;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

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
        FactoryBoy.lazyDelete();
        user = FactoryBoy.create(User.class);
        order = FactoryBoy.create(Order.class);
        order.setUser(user.id, AccountType.CONSUMER);
        order.needPay = BigDecimal.TEN;
        order.payMethod = "balance";
        order.status = OrderStatus.UNPAID;
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

    @Test
    public void test_展示确认支付信息页() {
        Http.Response response = GET("/payment_info/" + order.orderNumber);
        assertStatus(200, response);
        Order order1 = (Order) renderArgs("order");
        Account account1 = (Account) renderArgs("account");
        assertEquals(order1, order);
        assertEquals(new BigDecimal("20.00"), account1.amount);
        assertContentMatch(" <button id=\"confirm-pay\" class=\"confirm-pay\" type=\"submit\"style=\"display:none\">", response);
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
        assertContentMatch(" <button id=\"confirm-pay\" class=\"confirm-pay\" type=\"submit\"style=\"display:block\">", response);
        assertContentMatch(" <div id=\"other-pay\" class=\"onlinepay\"style=\"display:none\">", response);
    }

    @Test
    public void test_测试支付0元() {
        order.needPay = BigDecimal.ZERO;
        order.save();
        OrderItems orderItems = FactoryBoy.create(OrderItems.class);
        orderItems.order = order;
        orderItems.save();
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
        assertContentMatch("恭喜您付款成功！", response);
    }

    @Test
    public void test_测试使用部分余额支付_网银支付显示() {
        order.needPay = new BigDecimal("50");
        order.save();
        Http.Response response = GET("/payment_info/" + order.orderNumber);
        assertStatus(200, response);
        assertContentMatch(" <div id=\"other-pay\" class=\"onlinepay\"style=\"display:block\">", response);
        assertContentMatch("用余额付款<em>20.00</em>元", response);
        assertContentMatch("剩余 <strong>30.00</strong> 元可选择其他付款方式付款", response);
    }

    @Test
    public void test_测试使用部分余额支付() {
        order.needPay = new BigDecimal("50");
        OrderItems orderItems = FactoryBoy.create(OrderItems.class);
        orderItems.order = order;
        orderItems.save();
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
