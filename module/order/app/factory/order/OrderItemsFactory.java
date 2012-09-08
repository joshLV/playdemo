package factory.order;

<<<<<<< HEAD
import models.consumer.User;
=======
import java.util.ArrayList;

>>>>>>> REEB-957 实现Goods上得到库存的方法并缓存
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
