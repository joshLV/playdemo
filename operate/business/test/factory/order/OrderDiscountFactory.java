package factory.order;

import models.order.DiscountCode;
import models.order.Order;
import models.order.OrderDiscount;
import factory.FactoryBoy;
import factory.ModelFactory;

public class OrderDiscountFactory extends ModelFactory<OrderDiscount> {

    @Override
    public OrderDiscount define() {
        OrderDiscount od = new OrderDiscount();
        od.order = FactoryBoy.create(Order.class);
        od.discountCode = FactoryBoy.create(DiscountCode.class);
        od.discountAmount = od.discountCode.discountAmount;
        return od;
    }

}
