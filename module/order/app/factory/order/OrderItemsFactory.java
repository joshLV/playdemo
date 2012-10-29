package factory.order;

import models.consumer.User;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Goods;
import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;

import java.math.BigDecimal;

public class OrderItemsFactory extends ModelFactory<OrderItems> {

    @Override
    public OrderItems define() {
        Goods goods = FactoryBoy.lastOrCreate(Goods.class);
        User user = FactoryBoy.lastOrCreate(User.class);  //购买用户
        Order order = FactoryBoy.lastOrCreate(Order.class);
        return new OrderItems(order, goods, 1l, user.mobile, goods.salePrice, goods.salePrice);
    }

    @Factory(name = "orderItemReal")
    public OrderItems defineOrderItemReal(OrderItems orderItem) {
        orderItem.buyNumber = 2l;
        orderItem.faceValue = BigDecimal.valueOf(20);
        orderItem.originalPrice = BigDecimal.valueOf(8);
        orderItem.resalerPrice = BigDecimal.valueOf(10);
        orderItem.salePrice = BigDecimal.valueOf(15);
        return orderItem;
    }

    @Factory(name = "orderItemElectric")
    public OrderItems defineOrderItemElectric(OrderItems orderItem) {
        orderItem.buyNumber = 1l;
        orderItem.faceValue = BigDecimal.valueOf(15);
        orderItem.originalPrice = BigDecimal.valueOf(5);
        orderItem.resalerPrice = BigDecimal.valueOf(8);
        orderItem.salePrice = BigDecimal.valueOf(10);
        return orderItem;
    }
}
