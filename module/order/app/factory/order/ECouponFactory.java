package factory.order;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Goods;
import models.sales.Shop;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-20
 * Time: 下午7:33
 * To change this template use File | Settings | File Templates.
 */
public class ECouponFactory extends ModelFactory<ECoupon> {

    @Override
    public ECoupon define(){
        Order order = FactoryBoy.lastOrCreate(Order.class);
        OrderItems orderItems = FactoryBoy.create(OrderItems.class);
        Shop shop = FactoryBoy.lastOrCreate(Shop.class);
        Goods goods = FactoryBoy.lastOrCreate(Goods.class);
        ECoupon eCoupon = new ECoupon(order,goods,orderItems);
        eCoupon.shop = shop;
        return eCoupon;

    }

    @Factory(name = "Id")
    public ECoupon defineWithId(ECoupon eCoupon){
        eCoupon.order = FactoryBoy.lastOrCreate(Order.class);
        eCoupon.orderItems = FactoryBoy.lastOrCreate(OrderItems.class);
        return eCoupon;
    }
}
