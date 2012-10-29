package factory.order;

import java.math.BigDecimal;
import java.util.Date;

import models.accounts.AccountType;
import models.consumer.User;
import models.order.Order;
import models.order.OrderStatus;
import models.order.OrderType;

import com.uhuila.common.constants.DeletedStatus;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;

public class OrderFactory extends ModelFactory<Order> {

    /**
     * 未支付状态的订单
     *
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

    @Factory(name = "charge")
    public void defineChargeOrder(Order order) {
        order.accountPay = BigDecimal.ZERO;
        order.needPay = new BigDecimal("100");
        order.orderType = OrderType.CHARGE;
    }

    @Factory(name = "orderForCommissionsTest")
    public Order defineCommissionsOrder(Order order) {
        order.status = OrderStatus.UNPAID;
        order.amount = BigDecimal.valueOf(46);
        order.accountPay = BigDecimal.valueOf(10);
        order.discountPay = BigDecimal.valueOf(36);
        order.promotionBalancePay = BigDecimal.valueOf(0);
        order.needPay = BigDecimal.valueOf(46);
        order.freight = BigDecimal.valueOf(6);
        order.payMethod = "alipay";
        order.orderType = OrderType.CONSUME;
        return order;
    }


}
