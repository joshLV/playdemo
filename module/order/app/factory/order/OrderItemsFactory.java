package factory.order;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Goods;
import models.sales.GoodsHistory;

public class OrderItemsFactory extends ModelFactory<OrderItems> {

    @Override
    public OrderItems define() {
        Goods goods = FactoryBoy.lastOrCreate(Goods.class);
        GoodsHistory goodsHistory=FactoryBoy.lastOrCreate(GoodsHistory.class);
        Order order = FactoryBoy.lastOrCreate(Order.class);
        return new OrderItems(order, goods, 1l, "13800000000", goods.salePrice, goods.salePrice);
    }

}
