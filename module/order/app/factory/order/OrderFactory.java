package factory.order;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import factory.resale.ResalerFactory;
import models.consumer.User;
import models.order.Order;
import models.order.OrderStatus;
import models.order.OrderType;
import models.resale.Resaler;
import util.DateHelper;

import java.math.BigDecimal;
import java.util.Date;

public class OrderFactory extends ModelFactory<Order> {

    /**
     * 未支付状态的订单
     *
     * @return
     */
    @Override
    public Order define() {
        Order order = new Order();
        order.consumerId = FactoryBoy.lastOrCreate(User.class).id;
        Resaler yibaiquan = ResalerFactory.getYibaiquanResaler();
        order.userId = yibaiquan.id;
        order.orderNumber = "abc"+FactoryBoy.sequence(Order.class);
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

    @Factory(name = "orderForAccountsTest")
    public Order defineCommissionsOrder(Order order) {
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

    @Factory(name = "paid")
    public Order defineOrderPaid(Order order) {
        order.orderNumber = "201202220324134991598"+FactoryBoy.sequence(Order.class);
        order.status = OrderStatus.PAID;
        order.amount = BigDecimal.valueOf(0);
        order.accountPay = BigDecimal.valueOf(0);
        order.discountPay = BigDecimal.valueOf(0);
        order.needPay = BigDecimal.valueOf(0);
        order.createdAt = DateHelper.t("2012-03-15 16:40:28");
        order.updatedAt = DateHelper.t("2012-03-15 16:40:28");
        order.receiverPhone = "13512341234";
        order.receiverAddress = "内蒙古 呼和浩特市 玉泉区 凌兆路";
        order.receiverName = "苏杰1";
        order.postcode = "200120";
        order.lockVersion = 0;
        order.payRequestId = 0l;
        order.orderType = OrderType.CONSUME;

        return order;

    }


}
