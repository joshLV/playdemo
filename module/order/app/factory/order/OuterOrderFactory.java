package factory.order;

import models.order.Order;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import factory.FactoryBoy;
import factory.ModelFactory;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-24
 * Time: 上午11:45
 */
public class OuterOrderFactory extends ModelFactory<OuterOrder> {
    @Override
    public OuterOrder define() {
        OuterOrder order = new OuterOrder();
        order.ybqOrder = FactoryBoy.lastOrCreate(Order.class);
        order.orderId= 12345678L;
        order.message = "";
        order.partner = OuterOrderPartner.DD;
        order.status = OuterOrderStatus.ORDER_COPY;
        return order;
    }
}
