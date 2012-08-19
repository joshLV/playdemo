package factory.order;

import java.math.BigDecimal;

import models.order.OrderDiscount;
import factory.ModelFactory;

public class OrderDiscountFactory extends ModelFactory<OrderDiscount> {

    @Override
    public OrderDiscount define() {
        OrderDiscount od = new OrderDiscount();
        // od.order = FactoryBoy.create(Order.class);
        // od.discountCode = FactoryBoy.create(DiscountCode.class);
        od.discountAmount = BigDecimal.TEN;
        return od;
    }

    @Override
    public void delete(OrderDiscount orderDiscount) {
        orderDiscount.order = null;
        orderDiscount.orderItem = null;
        orderDiscount.discountCode = null;
        orderDiscount.save();
        orderDiscount.delete();
    }
}
