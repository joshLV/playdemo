package factory.order;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.order.Order;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;

import java.util.Date;

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
        order.orderNumber = "12345678";
        order.message = "";
        order.partner = OuterOrderPartner.DD;
        order.status = OuterOrderStatus.ORDER_COPY;
        return order;
    }
}
