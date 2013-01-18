package functional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
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

import models.order.*;
import models.sales.Category;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.test.FunctionalTest;
import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;

/**
 * User: hejun
 * Date: 12-7-30
 */
<<<<<<< Updated upstream:website/home/test/functional/UserCouponsTest.java
public class UserCouponsTest extends FunctionalTest {
=======
public class  UserCouponsFuncTest extends FunctionalTest {
>>>>>>> Stashed changes:website/home/test/functional/UserCouponsFuncTest.java

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
    public void testIndexConditionNull() {
        Http.Response response = GET("/coupons");
        assertIsOk(response);
        assertEquals(1, ((JPAExtPaginator<ECoupon>) renderArgs("couponsList")).size());
    }

    @Test
    public void testApplyRefundNoCoupon() {
        Map<String, String> params = new HashMap<>();
        params.put("applyNote", "我要退款");
        Http.Response response = POST("/coupons/refund/" + ecoupon.id + 6l, params);
        assertStatus(404, response);
    }


    @Test
    public void testIndex() {
        Http.Response response = GET("/coupons?condition.goodsName=" + goods.name);
        assertIsOk(response);
        assertEquals(1, ((JPAExtPaginator<ECoupon>) renderArgs("couponsList")).size());
        assertEquals(goods.salePrice.setScale(2), ((JPAExtPaginator<ECoupon>) renderArgs("couponsList")).get(0).salePrice.setScale(2));
    }

    @Test
    public void testShowCouponShops() {
        Http.Response response = GET("/coupon/" + ecoupon.id + "/shops");
        assertIsOk(response);
        assertEquals(1, ((Collection<Shop>) renderArgs("shops")).size());
    }


    @Test
    public void testApplyRefund() {
        // 设置总账户中的余额
        BigDecimal amount = new BigDecimal("10000");
        Account account = AccountUtil.getPlatformIncomingAccount();
        account.amount = amount;
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
