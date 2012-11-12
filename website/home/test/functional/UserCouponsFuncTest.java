package functional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import models.accounts.Account;
import models.accounts.util.AccountUtil;
import models.consumer.User;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.mvc.Http;
import play.test.FunctionalTest;
import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;

/**
 * Created with IntelliJ IDEA.
 * User: Juno
 * Date: 12-7-30
 * Time: 下午2:12
 * To change this template use File | Settings | File Templates.
 */
public class UserCouponsFuncTest extends FunctionalTest {

    User user;
    Goods goods;
    Order order;
    OrderItems orderItems;
    ECoupon ecoupon;
    
    @Before
    public void setUp() {
        FactoryBoy.deleteAll();

        //设置虚拟登陆
        // 设置测试登录的用户名
        user = FactoryBoy.create(User.class);
        Security.setLoginUserForTest(user.loginName);
        
        FactoryBoy.create(Supplier.class);
        FactoryBoy.create(Shop.class);
        
        goods = FactoryBoy.create(Goods.class);
        order = FactoryBoy.create(Order.class, new BuildCallback<Order>() {
            @Override
            public void build(Order o) {
                o.userId = user.id;
            }
        });
        orderItems = FactoryBoy.create(OrderItems.class, new BuildCallback<OrderItems>() {
            @Override
            public void build(OrderItems oi) {
                oi.phone = user.mobile;
                oi.buyNumber = 3l;
            }
        });
        ecoupon = FactoryBoy.create(ECoupon.class);
    }
    
    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testApplyRefund() {
        // 设置总账户中的余额
        Account account = AccountUtil.getPlatformIncomingAccount();
        account.amount = new BigDecimal("100000000");
        account.save();


        Map<String, String> params = new HashMap<>();
        params.put("applyNote", "我要退款");

        Http.Response response = POST("/coupons/refund/" + ecoupon.id, params);
        assertIsOk(response);
        ecoupon.refresh();
        assertEquals(ECouponStatus.REFUND, ecoupon.status);
    }

    @Test
    public void testSendMessage() {
        Http.Response response = GET("/coupons-message/" + ecoupon.id + "/send");
        assertIsOk(response);
    }

}
