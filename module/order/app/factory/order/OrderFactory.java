package factory.order;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.ModelFactory;
import models.accounts.AccountType;
import models.consumer.User;
import models.order.Order;
import models.order.OrderStatus;

import java.math.BigDecimal;
import java.util.Date;

public class OrderFactory extends ModelFactory<Order> {

    /**
     * 未支付状态的订单
     * @return
     */
    @Override
    public Order define() {
        Order order = new Order();
        order.userId = FactoryBoy.lastOrCreate(User.class).id;
        order.userType = AccountType.CONSUMER;
        order.orderNumber = "abc";
        order.accountEmail = "test@test.com";
        order.accountPay = BigDecimal.ZERO;
        order.amount = BigDecimal.ZERO;
        order.buyerMobile = "13800000000";
        order.buyerPhone = "02132342134";
        order.discountPay = BigDecimal.ZERO;
        order.deleted = DeletedStatus.UN_DELETED;
        order.createdAt = new Date();
        order.status = OrderStatus.UNPAID;
        return order;
    }

}
