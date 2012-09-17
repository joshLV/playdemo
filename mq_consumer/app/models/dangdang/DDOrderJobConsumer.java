package models.dangdang;

import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.order.DeliveryType;
import models.order.NotEnoughInventoryException;
import models.order.Order;
import models.order.OrderItems;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.MaterialType;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-17
 * Time: 下午3:13
 */
@OnApplicationStart(async = true)
public class DDOrderJobConsumer extends RabbitMQConsumer<DDOrderJobMessage> {
    public static String DD_LOGIN_NAME = Play.configuration.getProperty("dangdang.resaler_login_name", "dangdang");

    @Override
    protected void consume(DDOrderJobMessage message) {
        //开启事务管理
        JPAPlugin.startTx(false);
        DDOrder ddOrder = DDOrder.find("byOrderId", message.getOrderId()).first();
        if (ddOrder == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Logger.info("can not sleep");
                JPAPlugin.closeTx(true);
                return;
            }
            ddOrder = DDOrder.find("byOrderId", message.getOrderId()).first();
            if (ddOrder == null) {
                Logger.error("order not found: %s", message.getOrderId());
                JPAPlugin.closeTx(true);
                return;
            }
        }

        if (ddOrder.status == DDOrderStatus.ORDER_FINISH) {
            Logger.info("job synced");
            JPAPlugin.closeTx(true);
            return;
        }
        try {
            DDOrder.em().refresh(ddOrder, LockModeType.PESSIMISTIC_WRITE);
        } catch (PersistenceException e) {
            //拿不到锁就放弃
            Logger.info("can not lock dd order %s", ddOrder.orderId);
            JPAPlugin.closeTx(true);
            return;
        }
        if (ddOrder.status == DDOrderStatus.ORDER_ACCEPT) {
            if (buildUhuilaOrder(ddOrder)) {
                ddOrder.status = DDOrderStatus.ORDER_SEND;
                ddOrder.save();
                DDOrderJobMessage syncMessage = new DDOrderJobMessage(ddOrder.orderId);
                DDOrderQueueUtil.addJob(syncMessage);
            }
        } else if (ddOrder.status == DDOrderStatus.ORDER_SEND) {
            if (syncWithDD(ddOrder)) {
                ddOrder.status = DDOrderStatus.ORDER_FINISH;
                ddOrder.save();
            }
        }

        boolean rollBack = false;
        try {
            JPA.em().flush();
        } catch (RuntimeException e) {
            rollBack = true;
            Logger.info("update dd order status failed, will roll bace", e);
            //不抛异常 不让mq重试
        } finally {
            JPAPlugin.closeTx(rollBack);
        }
    }

    @Override
    protected Class getMessageType() {
        return DDOrderJobMessage.class;
    }

    @Override
    protected String queue() {
        return DDOrderQueueUtil.QUEUE_NAME;
    }

    private boolean syncWithDD(DDOrder ddOrder) {
        Order yqbOrder = Order.find("ddOrder=?", ddOrder).first();
        if (yqbOrder != null) {
            ddOrder.ybqOrder = yqbOrder;
            ddOrder.createAndUpdateInventory();
            return true;
        }
        return false;
    }

    private boolean buildUhuilaOrder(DDOrder order) {
        Logger.info("build uhuila order");
        Resaler resaler = Resaler.findOneByLoginName(DD_LOGIN_NAME);
        if (resaler == null) {
            Logger.error("can not find the resaler by login name: %s", DD_LOGIN_NAME);
            return false;
        }
        models.order.Order uhuilaOrder = models.order.Order.createConsumeOrder(resaler.getId(), AccountType.RESALER);
        uhuilaOrder.save();
        boolean containsElectronic = false;
        boolean containsReal = false;
        try {
            for (DDOrderItem orderItem : order.orderItems) {
                Goods goods = Goods.find("byId", orderItem.spgid).first();
                if (goods == null) {
                    Logger.info("goods not found: %s", orderItem.spgid);
                    return false;
                }

                OrderItems uhuilaOrderItem = uhuilaOrder.addOrderItem(
                        goods,
                        orderItem.orderItemNum,
                        order.receiveMobile,
                        orderItem.orderItemPrice,
                        orderItem.orderItemPrice
                );
                uhuilaOrderItem.save();
                if (goods.materialType.equals(MaterialType.REAL)) {
                    containsReal = true;
                } else if (goods.materialType.equals(MaterialType.ELECTRONIC)) {
                    containsElectronic = true;
                }
            }
        } catch (NotEnoughInventoryException e) {
            Logger.info("enventory not enough");
            return false;
        }
        if (containsElectronic) {
            uhuilaOrder.deliveryType = DeliveryType.SMS;
        } else if (containsReal) {
            uhuilaOrder.deliveryType = DeliveryType.LOGISTICS;
        }

        uhuilaOrder.createAndUpdateInventory();
        uhuilaOrder.accountPay = uhuilaOrder.needPay;
        uhuilaOrder.discountPay = BigDecimal.ZERO;
        uhuilaOrder.payMethod = PaymentSource.getBalanceSource().code;
        uhuilaOrder.payAndSendECoupon();
        uhuilaOrder.save();
        return true;
    }
}
