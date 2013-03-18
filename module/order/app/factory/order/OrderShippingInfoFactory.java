package factory.order;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.order.ExpressCompany;
import models.order.OrderItems;
import models.order.OrderShippingInfo;
import models.order.OuterOrder;

/**
 * User: tanglq
 * Date: 13-3-13
 * Time: 下午6:50
 */
public class OrderShippingInfoFactory extends ModelFactory<OrderShippingInfo> {
    @Override
    public OrderShippingInfo define() {
        OrderItems orderItems = FactoryBoy.lastOrCreate(OrderItems.class);
        OrderShippingInfo shippingInfo = new OrderShippingInfo();
        shippingInfo.address = "上海市徐汇区测试地址" + FactoryBoy.sequence(OrderShippingInfo.class) + "号";
        shippingInfo.buyNumber = orderItems.buyNumber;
        shippingInfo.salePrice = orderItems.salePrice;
        shippingInfo.paidAt = orderItems.order.paidAt;
        shippingInfo.tel = "021-31343113";
        shippingInfo.phone = "15383431311";
        shippingInfo.receiver = "张三";
        shippingInfo.outerOrderId = FactoryBoy.lastOrCreate(OuterOrder.class).orderId;
        shippingInfo.expressCompany = FactoryBoy.lastOrCreate(ExpressCompany.class);
        return shippingInfo;
    }
}
