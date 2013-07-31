package unit;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.order.ECoupon;
import models.sales.Goods;
import models.sales.Shop;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

/**
 * User: hejun
 * Date: 12-8-21
 * Time: 上午9:16
 */
public class ECouponUnitFactoryTest extends UnitTest {

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
    }

    @Test
    public void testQuery() {
        final Goods goods = FactoryBoy.create(Goods.class);
        final Shop shop = FactoryBoy.create(Shop.class, "SupplierId", new BuildCallback<Shop>() {
            @Override
            public void build(Shop target) {
                target.supplierId = goods.supplierId;
            }
        });
        ECoupon eCoupon = FactoryBoy.create(ECoupon.class, "Id", new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.shop = shop;
                target.goods = goods;
            }
        });

//        ECoupon eCouponQueried = ECoupon.query(eCoupon.eCouponSn, eCoupon.shop.supplierId);
//        assertEquals(eCouponQueried, eCoupon);

    }
}
