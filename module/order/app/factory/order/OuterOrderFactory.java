package factory.order;

import factory.ModelFactory;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;

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
        order.lockVersion = 0;
        order.message = "";
        order.partner = OuterOrderPartner.DD;
        order.createdAt = new Date();
        return order;
    }


}
