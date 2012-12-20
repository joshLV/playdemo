package models.yihaodian;

import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.order.DeliveryType;
import models.order.NotEnoughInventoryException;
import models.order.OrderItems;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.MaterialType;
import models.supplier.Supplier;
import models.yihaodian.shop.*;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 */
@OnApplicationStart(async = true)
public class YihaodianJobConsumer extends RabbitMQConsumer<YihaodianJobMessage>{
    public static String YHD_LOGIN_NAME = Resaler.YHD_LOGIN_NAME;
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
                JPAPlugin.closeTx(true);
                return;
            }
            yihaodianOrder = YihaodianOrder.find("byOrderId", message.getOrderId()).first();
            if (yihaodianOrder == null) {
                Logger.info("order not found: %s, maybe processed by other MQ.", message.getOrderId());
                JPAPlugin.closeTx(true);
                return;
            }
        }

        if(yihaodianOrder.jobFlag == JobFlag.SEND_SYNCED){
            Logger.info("job synced");
            JPAPlugin.closeTx(true);
            return;
        }

        //查看是否有实体券，如果全部都是的话就直接将此订单置为忽略状态，并结束任务
        int realItems =0;
        for (OrderItem orderItem : yihaodianOrder.orderItems) {
            if (orderItem.outerId == null) {
                realItems += 1;
            }
        }
        if (realItems == yihaodianOrder.orderItems.size()) {
            yihaodianOrder.jobFlag = JobFlag.IGNORE;
            yihaodianOrder.save();
            JPAPlugin.closeTx(false);
            return;
        }

        try{
            YihaodianOrder.em().refresh(yihaodianOrder, LockModeType.PESSIMISTIC_WRITE);
        }catch (PersistenceException e){
            //拿不到锁就放弃
            Logger.info("can not lock yihaodian order %s", yihaodianOrder.orderCode);
            JPAPlugin.closeTx(true);
            return;
        }
        if(yihaodianOrder.jobFlag == JobFlag.SEND_COPY){
            //等 1 分钟再发货
            if(yihaodianOrder.createdAt.getTime() < (System.currentTimeMillis() - 60000)){
                //如果用户没有取消订单再发货
                //首先刷新最新的订单
                YihaodianOrder refreshOrder = refreshYihaodianOrder(yihaodianOrder);
                if(refreshOrder != null){
                    if (refreshOrder.orderStatus != OrderStatus.ORDER_CANCEL) {
                        models.order.Order ybqOrder = null;
                        if(yihaodianOrder.ybqOrderId == null) {
                            ybqOrder = buildYibaiquanOrder(yihaodianOrder);
                        } else {
                            ybqOrder = models.order.Order.findById(yihaodianOrder.ybqOrderId);
                        }
                        if( ybqOrder != null){
                            if (realItems > 0) {
                                yihaodianOrder.jobFlag = JobFlag.IGNORE;

                                MailMessage mailMessage = new MailMessage();
                                mailMessage.addRecipient("op@uhuila.com");
                                mailMessage.setSubject("一号店实体券订单");
                                mailMessage.putParam("yhdOrderId", yihaodianOrder.orderId);
                                mailMessage.setTemplate("yihaodianRealGoods");
                                MailUtil.sendCommonMail(mailMessage);
                            }else {
                                yihaodianOrder.jobFlag = JobFlag.SEND_DONE;
                                YihaodianJobMessage syncMessage = new YihaodianJobMessage(yihaodianOrder.orderId);
                                YihaodianQueueUtil.addJob(syncMessage);
                            }
                            yihaodianOrder.ybqOrderId = ybqOrder.getId();
                            yihaodianOrder.save();
                        }
                    }else {
                        yihaodianOrder.jobFlag = JobFlag.CANCEL_SYNCED;
                        yihaodianOrder.save();
                    }
                }
            }
        }else if(yihaodianOrder.jobFlag == JobFlag.SEND_DONE){
            if (realItems > 0) {
                yihaodianOrder.jobFlag = JobFlag.IGNORE;
                yihaodianOrder.save();
            }else {
                if(syncWithYihaodian(yihaodianOrder)){
                    yihaodianOrder.jobFlag = JobFlag.SEND_SYNCED;
                    yihaodianOrder.save();
                }
            }
        }

        boolean rollBack = false;
        try {
            JPA.em().flush();
        } catch (RuntimeException e) {
            rollBack = true;
            Logger.info("update yihaodian order status failed, will roll back", e);
            //不抛异常 不让mq重试
        } finally {
            JPAPlugin.closeTx(rollBack);
        }
    }

    private boolean syncWithYihaodian(YihaodianOrder yihaodianOrder) {
        Map<String, String> params = new HashMap<>();
        params.put("orderCode", yihaodianOrder.orderCode);
        params.put("deliverySupplierId", DELIVERY_SUPPLIER);//测试公司
        params.put("expressNbr", yihaodianOrder.orderCode);
        Logger.info("yhd.logistics.order.shipments.update orderCode %s", params.get("orderCode"));
        Logger.info("yhd.logistics.order.shipments.update deliverySupplierId %s", params.get("deliverySupplierId"));
        Logger.info("yhd.logistics.order.shipments.update expressNbr", params.get("expressNbr"));

        String responseXml = YHDUtil.sendRequest(params, "yhd.logistics.order.shipments.update");
        Logger.info("yhd.logistics.order.shipments.update response %s", responseXml);
        if (responseXml != null) {
            YHDResponse<UpdateResult> res = new YHDResponse<>();
            res.parseXml(responseXml, "updateCount", false, UpdateResult.parser);
            if(res.getErrorCount() == 0){
                return true;
            }else {
                Logger.info("sync With yihaodian error");
                return checkYihaodianSent(yihaodianOrder);
            }
        }
        return false;
    }

    private YihaodianOrder refreshYihaodianOrder(YihaodianOrder yihaodianOrder){
        Logger.info("start check yihaodian sent %s" , yihaodianOrder.orderCode);
        Map<String, String> params = new HashMap<>();
        params.put("orderCodeList", yihaodianOrder.orderCode);
        Logger.info("yhd.orders.detail.get orderCodeList %s", params.get("orderCodeList"));

        String responseXml = YHDUtil.sendRequest(params, "yhd.orders.detail.get");
        Logger.info("yhd.orders.detail.get response %s", responseXml);
        if (responseXml != null) {
            YHDResponse<YihaodianOrder> res = new YHDResponse<>();
            res.parseXml(responseXml, "orderInfoList", true, YihaodianOrder.fullParser);
            if(res.getErrorCount() == 0){
                List<YihaodianOrder> orders = res.getVs();
                if (orders.size() > 0){
                    return orders.get(0);
                }
            }
        }
        return null;
    }

    private boolean checkYihaodianSent(YihaodianOrder yihaodianOrder) {
        Logger.info("start check yihaodian sent %s" , yihaodianOrder.orderCode);
        Map<String, String> params = new HashMap<>();
        params.put("orderCodeList", yihaodianOrder.orderCode);
        Logger.info("yhd.orders.detail.get orderCodeList %s", params.get("orderCodeList"));

        String responseXml = YHDUtil.sendRequest(params, "yhd.orders.detail.get");
        Logger.info("yhd.orders.detail.get response %s", responseXml);
        if (responseXml != null) {
            YHDResponse<YihaodianOrder> res = new YHDResponse<>();
            res.parseXml(responseXml, "orderInfoList", true, YihaodianOrder.fullParser);
            if(res.getErrorCount() == 0){
                List<YihaodianOrder> orders = res.getVs();
                if (orders.size() > 0){
                    YihaodianOrder order = orders.get(0);
                    if (order.orderStatus != OrderStatus.ORDER_PAYED
                            && order.orderStatus != OrderStatus.ORDER_WAIT_PAY
                            && order.orderStatus != OrderStatus.ORDER_CANCEL) {
                        //只要不是待付款、已付款或取消状态状态，就说明我们已经以某种渠道发过货了
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private models.order.Order buildYibaiquanOrder(YihaodianOrder order) {
        Logger.info("build uhuila order");
        Resaler resaler = Resaler.findOneByLoginName(YHD_LOGIN_NAME);
        if (resaler == null){
            Logger.error("can not find the resaler by login name: %s", YHD_LOGIN_NAME);
            return null;
        }
        models.order.Order uhuilaOrder = models.order.Order.createConsumeOrder(resaler.getId(), AccountType.RESALER);
        uhuilaOrder.save();
        boolean containsElectronic = false;
        boolean containsReal = false;
        try {
            boolean hasElectronicOrderItem = false;
            for (OrderItem orderItem : order.orderItems){
                if (orderItem.outerId == null) {
                    continue;//实体券无视
                }else if (!hasElectronicOrderItem){
                    hasElectronicOrderItem = true;
                }
                Goods goods = Goods.find("byId", orderItem.outerId).first();
                if(goods == null){
                    Logger.info("goods not found: %s", orderItem.outerId );
                    return null;
                }

                OrderItems uhuilaOrderItem  = uhuilaOrder.addOrderItem(
                        goods,
                        orderItem.orderItemNum,
                        order.goodReceiverMobile,
                        orderItem.orderItemPrice,
                        orderItem.orderItemPrice
                );
                uhuilaOrderItem.save();
                if(goods.materialType.equals(MaterialType.REAL)){
                    containsReal = true;
                }else if (goods.materialType.equals(MaterialType.ELECTRONIC)) {
                    containsElectronic = true;
                }
            }
            if(!hasElectronicOrderItem) {
                Logger.info("has no electronic order item");
                return null;
            }
        } catch (NotEnoughInventoryException e) {
            Logger.info("enventory not enough");
            JPA.em().getTransaction().rollback();
            return null;
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
        return uhuilaOrder;
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
