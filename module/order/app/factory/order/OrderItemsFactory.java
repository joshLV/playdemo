package factory.order;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Goods;

public class OrderItemsFactory extends ModelFactory<OrderItems> {

    @Override
    public OrderItems define() {
        Goods goods = FactoryBoy.create(Goods.class);
        Order order = FactoryBoy.create(Order.class);
        OrderItems item = new OrderItems(order, goods, 1l, "13800000000", goods.salePrice, goods.salePrice);
        return item;
    }

}
