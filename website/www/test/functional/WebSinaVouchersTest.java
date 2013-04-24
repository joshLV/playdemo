package functional;

import com.uhuila.common.util.DateUtil;
import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.resale.ResalerFactory;
import models.accounts.PaymentSource;
import models.consumer.User;
import models.ktv.KtvPriceSchedule;
import models.ktv.KtvRoom;
import models.ktv.KtvRoomOrderInfo;
import models.order.*;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.ResalerProduct;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.data.validation.Error;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;
import util.DateHelper;

import java.util.*;

/**
 * User: yan
 * Date: 13-4-1
 * Time: 下午4:58
 */
public class WebSinaVouchersTest extends FunctionalTest {

    ResalerProduct product;
    User user;
    Resaler sinaResaler;

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
        PaymentSource source = FactoryBoy.create(PaymentSource.class);
        source.code = "sina";
        source.paymentCode = "sina";
        source.save();
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        sinaResaler = ResalerFactory.getSinaResaler();
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
        order.userId = sinaResaler.id;
        order.save();
        FactoryBoy.batchCreate(10, ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.goods = product.goods;
                target.partner = ECouponPartner.SINA;
                target.order = order;
            }
        });
        Http.Response response = GET(Router.reverse("WebUserSinaVouchers.myCoupons"));
        assertIsOk(response);
        List<ECoupon> couponList = (List) renderArgs("couponList");
        assertEquals(5, couponList.size());
        assertEquals(user, renderArgs("user"));
    }

    @Test
    public void testShowMoreCoupon() {
        final Order order = FactoryBoy.create(Order.class);
        order.consumerId = user.id;
        order.userId = sinaResaler.id;
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
        order.userId = sinaResaler.id;
        order.save();
        ECoupon coupon1 = FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.goods = product.goods;
                target.partner = ECouponPartner.SINA;
                target.order = order;
            }
        });
        Http.Response response = GET(Router.reverse("WebUserSinaVouchers.showDetail").url + "?couponId=" + coupon1.id);
        assertIsOk(response);
        ECoupon coupon = (ECoupon) renderArgs("coupon");
        assertEquals(coupon, renderArgs("coupon"));
    }

    @Test
    public void testCreateOrder_multRoom() {
        Supplier supplier = FactoryBoy.create(Supplier.class);
        supplier.setProperty("ktvSupplier", "1");

        product.goods.supplierId = supplier.id;
        product.goods.save();
        product.save();
        KtvRoom ktvRoomA = FactoryBoy.create(KtvRoom.class);
        KtvRoom ktvRoomB = FactoryBoy.create(KtvRoom.class);

        KtvPriceSchedule schedule = FactoryBoy.create(KtvPriceSchedule.class);
        schedule.roomType = ktvRoomA.roomType;
        schedule.save();

//        KtvPriceSchedule scheduleB = FactoryBoy.create(KtvPriceSchedule.class);
//        scheduleB.roomType = ktvRoomB.roomType;
//        scheduleB.save();

        Map<String, String> params = new HashMap<>();
        params.put("productId", product.partnerProductId);
        params.put("scheduledDay", DateUtil.dateToString(new Date(), 0));
        params.put("phone", "13231283912");
        params.put("roomId" + ktvRoomA.id, "08:00");
        params.put("roomId" + ktvRoomB.id, "09:00");
        Http.Response response = POST(Router.reverse("WebSinaVouchers.order"), params);
        assertStatus(200, response);
        assertEquals(1, Order.count());
        assertEquals(2, OrderItems.count());
        assertEquals(2, KtvRoomOrderInfo.count());

    }

    @Test
    public void testCreateOrder_singleRoom() {
        Supplier supplier = FactoryBoy.create(Supplier.class);
        supplier.setProperty("ktvSupplier", "1");

        product.goods.supplierId = supplier.id;
        product.goods.save();
        product.save();
        KtvRoom ktvRoomA = FactoryBoy.create(KtvRoom.class);

        KtvPriceSchedule schedule = FactoryBoy.create(KtvPriceSchedule.class);
        schedule.roomType = ktvRoomA.roomType;
        schedule.save();

        Map<String, String> params = new HashMap<>();
        params.put("productId", product.partnerProductId);
        params.put("scheduledDay", DateUtil.dateToString(new Date(), 0));
        params.put("phone", "13231283912");
        params.put("roomId" + ktvRoomA.id, "08:00,09:00");
        Http.Response response = POST(Router.reverse("WebSinaVouchers.order"), params);
        assertStatus(200, response);
        assertEquals(1, Order.count());
        assertEquals(1, OrderItems.count());
        assertEquals(2, KtvRoomOrderInfo.count());

    }
}
