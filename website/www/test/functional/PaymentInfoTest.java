package functional;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.consumer.User;
import models.order.Order;
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
    }


    @Test
    public void test_用余额支付的订单() {
        Map<String, String> params = new HashMap<>();
        params.put("orderNumber", order.orderNumber);
        params.put("useBalance", "false");
        params.put("paymentSourceCode", "balance");
        Http.Response response = POST("/payment_info/confirm", params);
        assertStatus(200, response);
        Order order1 = (Order) renderArgs("order");
        String paymentSource=(String)renderArgs("paymentSource");
        assertEquals(order1, order);
        assertEquals(order.payMethod, paymentSource);
    }
}
