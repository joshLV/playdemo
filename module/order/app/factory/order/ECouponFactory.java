package factory.order;

import com.uhuila.common.util.DateUtil;
import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Goods;

import java.math.BigDecimal;

/**
 * User: hejun
 * Date: 12-8-20
 * Time: 下午7:33
 */
public class ECouponFactory extends ModelFactory<ECoupon> {

    @Override
    public ECoupon define() {
        Goods goods = FactoryBoy.lastOrCreate(Goods.class);
        Order order = FactoryBoy.lastOrCreate(Order.class);
        OrderItems orderItems = FactoryBoy.lastOrCreate(OrderItems.class);

        return new ECoupon(order, goods, orderItems);
    }

    @Factory(name = "Id")
    public ECoupon defineWithId(ECoupon eCoupon) {
        eCoupon.order = FactoryBoy.lastOrCreate(Order.class);
        eCoupon.orderItems = FactoryBoy.lastOrCreate(OrderItems.class);
        return eCoupon;
    }

    @Factory(name = "couponForCommissionsTest")
    public ECoupon defineCouponForCommissionsTest(ECoupon eCoupon) {
        eCoupon.eCouponSn = "1234566001";
        eCoupon.effectiveAt = DateUtil.getBeginOfDay();
        eCoupon.faceValue = BigDecimal.valueOf(15);
        eCoupon.originalPrice = BigDecimal.valueOf(5);
        eCoupon.resalerPrice = BigDecimal.valueOf(8);
        eCoupon.salePrice = BigDecimal.valueOf(10);
        return eCoupon;
    }


}
