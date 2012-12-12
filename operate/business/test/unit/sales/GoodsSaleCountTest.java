package unit.sales;

import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.sales.Goods;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import factory.FactoryBoy;
import factory.callback.BuildCallback;

public class GoodsSaleCountTest extends UnitTest {

    @Before
    public void setUp() {
<<<<<<< Updated upstream
        FactoryBoy.deleteAll();
=======
        FactoryBoy.lazyDelete();
>>>>>>> Stashed changes
    }

    @Test
    public void 当订单修改时会影响已销售数据() {
        OrderItems orderItems = FactoryBoy.create(OrderItems.class);
        Goods goods = orderItems.goods;
<<<<<<< Updated upstream
        goods.cumulativeStocks = 20l;
        goods.save();

        Order order = orderItems.order;
        order.status = OrderStatus.PAID;
        order.save();
        assertEquals(new Long(1), goods.getRealSaleCount());
        assertEquals(new Long(19), goods.getRealStocks());
        order.refresh();
=======
        goods.baseSale = 20l;
        goods.save();
        Order order = orderItems.order;
        order.status = OrderStatus.PAID;
        order.save();
        assertEquals(new Long(1), goods.getRealSaleCount());
        assertEquals(new Long(19), goods.getRealStocks());

>>>>>>> Stashed changes
        // 取消订单后库存释放
        assertEquals(1, order.orderItems.size());
        order.status = OrderStatus.CANCELED;
        order.save();

        assertEquals(new Long(0), goods.getRealSaleCount());
        assertEquals(new Long(20), goods.getRealStocks());
    }

<<<<<<< Updated upstream

=======
>>>>>>> Stashed changes
    @Test
    public void 当券退款时会影响已销售数据() {
        ECoupon ecoupon = FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.status = ECouponStatus.UNCONSUMED;
            }
        });
        Goods goods = ecoupon.goods;
<<<<<<< Updated upstream
        goods.cumulativeStocks= 20l;
=======
        goods.baseSale = 20l;
>>>>>>> Stashed changes
        goods.save();
        Order order = ecoupon.order;
        order.status = OrderStatus.PAID;
        order.save();

        assertEquals(new Long(1), goods.getRealSaleCount());
        assertEquals(new Long(19), goods.getRealStocks());

        // 券退款后库存释放
        ecoupon.status = ECouponStatus.REFUND;
        ecoupon.save();

        assertEquals(new Long(0), goods.getRealSaleCount());
        assertEquals(new Long(20), goods.getRealStocks());
    }
}
