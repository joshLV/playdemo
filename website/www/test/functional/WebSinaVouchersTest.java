package functional;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.accounts.AccountType;
import models.consumer.User;
import models.order.ECoupon;
import models.order.ECouponPartner;
import models.order.Order;
import models.order.OuterOrderPartner;
import models.sales.Goods;
import models.sales.ResalerProduct;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;

import play.data.validation.Error;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: yan
 * Date: 13-4-1
 * Time: 下午4:58
 */
public class WebSinaVouchersTest extends FunctionalTest {

    ResalerProduct product;
    User user;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        final Goods goods = FactoryBoy.create(Goods.class);
        product = FactoryBoy.create(ResalerProduct.class, new BuildCallback<ResalerProduct>() {
            @Override
            public void build(ResalerProduct target) {
                target.partner = OuterOrderPartner.SINA;
                target.partnerProductId = "123";
                target.goods = goods;
            }
        });

        user = FactoryBoy.create(User.class);

        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @Test
    public void testShowUpload() {
        Http.Response response = GET("/weibo/wap/product/" + product.partnerProductId);
        assertIsOk(response);

        assertEquals(product.goods, renderArgs("goods"));
        assertEquals(product.partnerProductId, renderArgs("productId"));
    }

    @Test
    public void testShowOrder() {
        Http.Response response = GET("/weibo/wap/order?productId=" + product.partnerProductId);
        assertIsOk(response);

        assertEquals(user, renderArgs("user"));
        assertEquals(product.goods, renderArgs("goods"));
        assertEquals(product.partnerProductId, renderArgs("productId"));
    }

    @Test
    public void testCreateOrder_noPhone() {
        Map<String, String> params = new HashMap<>();
        params.put("productId", product.partnerProductId);
        params.put("buyCount", "1");
        params.put("source", "wap");
        Http.Response response = POST("/weibo/wap/order", params);
        assertIsOk(response);
        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("phone", errors.get(0).getKey());
    }

    @Test
    public void testCreateOrder_ValidPhone() {
        Map<String, String> params = new HashMap<>();
        params.put("productId", product.partnerProductId);
        params.put("phone", "1351234a");
        params.put("buyCount", "1");
        params.put("source", "wap");
        Http.Response response = POST("/weibo/wap/order", params);
        assertIsOk(response);
        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("phone", errors.get(0).getKey());
    }

    @Test
    public void testCreateOrder_NoBuycount() {
        Map<String, String> params = new HashMap<>();
        params.put("productId", product.partnerProductId);
        params.put("phone", "13512345678");
        params.put("source", "wap");
        Http.Response response = POST("/weibo/wap/order", params);
        assertIsOk(response);
        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("buyCount", errors.get(0).getKey());
    }

    @Test
    public void testShowCoupon() {
        final Order order = FactoryBoy.create(Order.class);
        order.consumerId = user.id;
        order.userType = AccountType.RESALER;
        order.userId = 1l;
        order.save();
        FactoryBoy.batchCreate(10, ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.goods = product.goods;
                target.partner = ECouponPartner.SINA;
                target.order = order;
            }
        });
        Http.Response response = GET(Router.reverse("WebUserSinaVouchers.showCoupon"));
        assertIsOk(response);
        List<ECoupon> couponList = (List) renderArgs("couponList");
        assertEquals(5, couponList.size());
        assertEquals(user, renderArgs("user"));
    }
    @Test
    public void testShowMoreCoupon() {
        final Order order = FactoryBoy.create(Order.class);
        order.consumerId = user.id;
        order.userType = AccountType.RESALER;
        order.userId = 1l;
        order.save();
        FactoryBoy.batchCreate(10, ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.goods = product.goods;
                target.partner = ECouponPartner.SINA;
                target.order = order;
            }
        });
        Http.Response response = GET(Router.reverse("WebUserSinaVouchers.showMoreCoupon"));
        assertIsOk(response);
        List<ECoupon> couponList = (List) renderArgs("couponList");
        assertEquals(10, couponList.size());
        assertEquals(user, renderArgs("user"));
    }
    @Test
    public void testShowDetail() {
        final Order order = FactoryBoy.create(Order.class);
        order.consumerId = user.id;
        order.userType = AccountType.RESALER;
        order.userId = 1l;
        order.save();
        FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.goods = product.goods;
                target.partner = ECouponPartner.SINA;
                target.order = order;
            }
        });
        Http.Response response = GET(Router.reverse("WebUserSinaVouchers.showDetail"));
        assertIsOk(response);
        ECoupon coupon=(ECoupon) renderArgs("coupon");
        assertEquals(coupon,renderArgs("coupon"));
    }

}
