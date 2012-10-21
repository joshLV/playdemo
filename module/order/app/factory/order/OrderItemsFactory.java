package factory.order;

import models.consumer.User;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Goods;
import factory.FactoryBoy;
import factory.ModelFactory;

public class OrderItemsFactory extends ModelFactory<OrderItems> {

    @Override
    public OrderItems define() {
        Goods goods = FactoryBoy.lastOrCreate(Goods.class);
        User user = FactoryBoy.lastOrCreate(User.class);  //购买用户
        Order order = FactoryBoy.lastOrCreate(Order.class);
        return new OrderItems(order, goods, 1l, user.mobile, goods.salePrice, goods.salePrice);
    }

}
