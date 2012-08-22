package factory.order;

import com.uhuila.common.constants.DeletedStatus;
import factory.ModelFactory;
import models.order.Order;

import java.math.BigDecimal;
import java.util.Date;

public class OrderFactory extends ModelFactory<Order> {

    @Override
    public Order define() {
        Order order = new Order();
        order.accountEmail = "test@test.com";
        order.accountPay = BigDecimal.ZERO;
        order.amount = BigDecimal.ZERO;
        order.buyerMobile = "13800000000";
        order.buyerPhone = "02132342134";
        order.discountPay = BigDecimal.ZERO;
        order.deleted = DeletedStatus.UN_DELETED;
        order.createdAt = new Date();
        return order;
    }

}
