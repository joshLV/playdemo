package function;

import controllers.modules.resale.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.order.CouponsCondition;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.Shop;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import util.DateHelper;

public class ResaleCouponTest extends FunctionalTest {
    Shop shop;
    Goods goods;
    ECoupon coupon;
    Resaler resaler;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();

        resaler = FactoryBoy.create(Resaler.class);
        shop = FactoryBoy.create(Shop.class);
        goods = FactoryBoy.create(Goods.class);

        FactoryBoy.create(Order.class, new BuildCallback<Order>() {
            @Override
            public void build(Order o) {
                o.userId = resaler.id;
                o.paidAt = DateHelper.beforeDays(1);
            }
        });

        coupon = FactoryBoy.create(ECoupon.class);
        coupon.status = ECouponStatus.CONSUMED;
        coupon.save();
        Security.setLoginUserForTest(resaler.loginName);
    }

    @Test
    public void testCoupons() throws Exception {
        CouponsCondition condition = new CouponsCondition();
        condition.consumedAtBegin = DateHelper.beforeDays(4);
        Response response = GET("/ecoupons");
        assertIsOk(response);
        JPAExtPaginator<ECoupon> couponsList = (JPAExtPaginator<ECoupon>) renderArgs("couponsList");
        assertNotNull(couponsList);
        assertEquals(1, couponsList.getRowCount());
    }

}
