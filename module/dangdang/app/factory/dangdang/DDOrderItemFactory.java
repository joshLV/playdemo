package factory.dangdang;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.dangdang.DDOrderItem;
import models.order.OrderItems;
import models.sales.Goods;

import java.util.Date;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 9/25/12
 * Time: 6:24 PM
 */
public class DDOrderItemFactory extends ModelFactory<DDOrderItem> {
    @Override
    public DDOrderItem define() {
        OrderItems orderItems = FactoryBoy.create(OrderItems.class);
        Goods goods = FactoryBoy.create(Goods.class);
        DDOrderItem item = new DDOrderItem();
        item.ybqOrderItems = orderItems;
        item.createdAt = new Date();
        item.ddGoodsId = "123";
        item.ddOrderId = 1000l;
        item.goodsId = goods.id;
        return item;
    }
}
