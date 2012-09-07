package models.yihaodian;

import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.order.DeliveryType;
import models.order.NotEnoughInventoryException;
import models.order.OrderItems;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.MaterialType;
import org.dom4j.DocumentException;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

import javax.persistence.LockModeType;
import javax.persistence.LockTimeoutException;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 */
@OnApplicationStart(async = true)
public class YihaodianJobConsumer extends RabbitMQConsumer<YihaodianJobMessage>{
    public static String YHD_LOGIN_NAME = Play.configuration.getProperty("yihaodian.resaler_login_name", "yihaodian");
    public static String DELIVERY_SUPPLIER = Play.configuration.getProperty("yihaodian.delivery_supplier");

    @Override
    protected void consume(YihaodianJobMessage message) {
        //开启事务管理
        JPAPlugin.startTx(false);
        YihaodianOrder yihaodianOrder = YihaodianOrder.find("byOrderId", message.getOrderId()).first();
        if(yihaodianOrder == null){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Logger.info("can not sleep");
                return;
            }
            yihaodianOrder = YihaodianOrder.find("byOrderId", message.getOrderId()).first();
            if (yihaodianOrder == null) {
                Logger.error("order not found: %s", message.getOrderId());
                return;
            }
        }

        if(yihaodianOrder.jobFlag == JobFlag.SEND_SYNCED){
            Logger.info("job synced");
            return;
        }
        try{
            YihaodianOrder.em().refresh(yihaodianOrder, LockModeType.PESSIMISTIC_WRITE);
        }catch (PersistenceException e){
            //拿不到锁就放弃
            return;
        }
        if(yihaodianOrder.jobFlag == JobFlag.SEND_COPY){
            if( buildUhuilaOrder(yihaodianOrder)){
                yihaodianOrder.jobFlag = JobFlag.SEND_DONE;
                yihaodianOrder.save();

                YihaodianJobMessage syncMessage = new YihaodianJobMessage(yihaodianOrder.orderId);
                YihaodianQueueUtil.addJob(syncMessage);
            }
        }else if(yihaodianOrder.jobFlag == JobFlag.SEND_DONE){
            if( syncWithYihaodian(yihaodianOrder)){
                yihaodianOrder.jobFlag = JobFlag.SEND_SYNCED;
                yihaodianOrder.save();
            }
        }

        boolean rollBack = false;
        try {
            // work with your models
            JPA.em().flush();
        } catch (RuntimeException e) {
            rollBack = true;
            // throw exception to prevent msg ACK, need to refine error handling :)

            //不抛异常 不让mq重试
//            throw e;
        } finally {
            JPAPlugin.closeTx(rollBack);
        }
    }

    private boolean syncWithYihaodian(YihaodianOrder yihaodianOrder) {
        Map<String, String> params = new HashMap<>();
        params.put("orderCode", yihaodianOrder.orderCode);
        params.put("deliverySupplierId", DELIVERY_SUPPLIER);//测试公司
        params.put("expressNbr", yihaodianOrder.orderCode);

        String responseXml = Util.sendRequest(params, "hd.logistics.order.shipments.update");
        Logger.info("hd.logistics.order.shipments.update %s", responseXml);
        if (responseXml != null) {
            Response<UpdateResult> res = new Response<>();
            res.parseXml(responseXml, "updateCount", false, UpdateResult.parser);
            if(res.getErrorCount() == 0){
                return true;
            }else {
                Logger.info("sync With yihaodian error");
            }
        }
        return false;
    }

    private boolean buildUhuilaOrder(YihaodianOrder order) {
        Logger.info("build uhuila order");
        Resaler resaler = Resaler.findOneByLoginName(YHD_LOGIN_NAME);
        if (resaler == null){
            Logger.error("can not find the resaler by login name: %s", YHD_LOGIN_NAME);
            return false;
        }
        models.order.Order uhuilaOrder = models.order.Order.createConsumeOrder(resaler.getId(), AccountType.RESALER);
        uhuilaOrder.save();
        boolean containsElectronic = false;
        boolean containsReal = false;
        try {
            for (OrderItem orderItem : order.orderItems){
                Goods goods = Goods.find("byId", orderItem.outerId).first();
                if(goods == null){
                    Logger.info("goods not found: %s", orderItem.outerId );
                    return false;
                }

                OrderItems uhuilaOrderItem  = uhuilaOrder.addOrderItem(
                        goods,
                        orderItem.orderItemNum,
                        order.goodReceiverMobile,
                        goods.salePrice, //最终成交价
                        goods.salePrice
                );
                uhuilaOrderItem.save();
                if(goods.materialType.equals(MaterialType.REAL)){
                    containsReal = true;
                }else if (goods.materialType.equals(MaterialType.ELECTRONIC)) {
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

    @Override
    protected Class getMessageType() {
        return YihaodianJobMessage.class;
    }

    @Override
    protected String queue() {
        return YihaodianQueueUtil.QUEUE_NAME;
    }
}
