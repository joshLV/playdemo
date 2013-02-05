package models.yihaodian;

import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.order.*;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.MaterialType;
import models.sales.ResalerProduct;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.jobs.OnApplicationStart;
import play.libs.XPath;
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
        OuterOrder outerOrder = OuterOrder.find("byOrderIdAndPartner", message.getOrderId(), OuterOrderPartner.YHD).first();
        if(outerOrder == null){
            JPAPlugin.closeTx(true);
            return;
        }

        Document yihaodianOrder = outerOrder.getMessageAsXmlDocument();
        List<Node> orderItems = XPath.selectNodes("orderItemList/orderItem", yihaodianOrder);

        int realItems =0;
        for (Node orderItem : orderItems) {
            String outerId = XPath.selectText("outerId", orderItem);
            if (outerId != null) {
                Goods goods = ResalerProduct.getGoods(Long.parseLong(outerId));
                if (goods != null && goods.materialType == MaterialType.REAL) {
                    realItems += 1;
                }
            }
        }
        if (realItems == orderItems.size()) {
            outerOrder.status = OuterOrderStatus.ORDER_IGNORE;
            outerOrder.save();
            JPAPlugin.closeTx(false);
            return;
        }

        try{
            OuterOrder.em().refresh(outerOrder, LockModeType.PESSIMISTIC_WRITE);
        }catch (PersistenceException e){
            //拿不到锁就放弃
            Logger.info("can not lock yihaodian order %s", message.getOrderId());
            JPAPlugin.closeTx(true);
            return;
        }
        String orderCode = XPath.selectText("orderDetail/orderCode", yihaodianOrder);
        if(outerOrder.status == OuterOrderStatus.ORDER_COPY){
            //等 1 分钟再发货
            if(outerOrder.createdAt.getTime() < (System.currentTimeMillis() - 60000)){
                //如果用户没有取消订单再发货
                //首先刷新最新的订单
                Node refreshOrder = refreshYihaodianOrder(orderCode);
                if(refreshOrder != null){
                    YHDOrderStatus orderStatus = YHDOrderStatus.valueOf(XPath.selectText("orderDetail/orderStatus", yihaodianOrder));
                    if (YHDOrderStatus.ORDER_CANCEL != orderStatus) {
                        if(outerOrder.ybqOrder == null) {
                            String goodsReceiverMobile = XPath.selectText("orderDetail/goodReceiverMoblie", yihaodianOrder);
                            outerOrder.ybqOrder = buildYibaiquanOrder(orderItems, goodsReceiverMobile);
                        }
                        if( outerOrder.ybqOrder != null){
                            if (realItems > 0) {
                                outerOrder.status = OuterOrderStatus.ORDER_IGNORE;

                                MailMessage mailMessage = new MailMessage();
                                mailMessage.addRecipient("op@uhuila.com");
                                mailMessage.setSubject("一号店实体券订单");
                                mailMessage.putParam("yhdOrderId", message.getOrderId());
                                mailMessage.setTemplate("yihaodianRealGoods");
                                MailUtil.sendCommonMail(mailMessage);
                            }else {
                                outerOrder.status = OuterOrderStatus.ORDER_DONE;
                                YihaodianJobMessage syncMessage = new YihaodianJobMessage(message.getOrderId());
                                YihaodianQueueUtil.addJob(syncMessage);
                            }
                            outerOrder.save();

                            List<ECoupon> couponList = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
                            for(ECoupon coupon : couponList) {
                                coupon.partner = ECouponPartner.YHD;
                                coupon.save();
                            }
                        }
                    }else {
                        outerOrder.status = OuterOrderStatus.ORDER_CANCELED;
                        outerOrder.save();
                    }
                }
            }
        }else if(outerOrder.status == OuterOrderStatus.ORDER_DONE){
            if (realItems > 0) {
                outerOrder.status = OuterOrderStatus.ORDER_IGNORE;
                outerOrder.save();
            }else {
                if(syncWithYihaodian(orderCode)){
                    outerOrder.status = OuterOrderStatus.ORDER_SYNCED;
                    outerOrder.save();
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

    private boolean syncWithYihaodian(String orderCode) {
        Map<String, String> params = new HashMap<>();
        params.put("orderCode", orderCode);
        params.put("deliverySupplierId", DELIVERY_SUPPLIER);//测试公司
        params.put("expressNbr", orderCode);

        YHDResponse response = YHDUtil.sendRequest(params, "yhd.logistics.order.shipments.update", "updateCount");
        return response.isOk() || checkYihaodianSent(orderCode);
    }

    private Node refreshYihaodianOrder(String orderCode){
        Map<String, String> params = new HashMap<>();
        params.put("orderCodeList", orderCode);

        YHDResponse response = YHDUtil.sendRequest(params, "yhd.order.detail.get", "orderInfo");
        if (response.isOk()) {
            return response.data;
        }
        return null;
    }

    private boolean checkYihaodianSent(String orderCode) {
        Map<String, String> params = new HashMap<>();
        params.put("orderCodeList", orderCode);

        YHDResponse response = YHDUtil.sendRequest(params, "yhd.order.detail.get", "orderInfo");
        if (response.isOk()) {
            YHDOrderStatus orderStatus = YHDOrderStatus.valueOf(XPath.selectText("orderDetail/orderStatus", response.data));
            if (orderStatus != YHDOrderStatus.ORDER_PAYED
                    && orderStatus != YHDOrderStatus.ORDER_WAIT_PAY
                    && orderStatus != YHDOrderStatus.ORDER_CANCEL) {
                //只要不是待付款、已付款或取消状态状态，就说明我们已经以某种渠道发过货了
                return true;
            }
        }
        return false;
    }

    private models.order.Order buildYibaiquanOrder(List<Node> orderItems, String goodReceiverMobile) {
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
            for (Node orderItem : orderItems){

                String outerId = XPath.selectText("outerId", orderItem);
                Goods goods = null;
                if (outerId != null) {
                    goods = ResalerProduct.getGoods(Long.parseLong(outerId));
                }else {
                    continue;
                }
                if (goods == null) {
                    Logger.info("yihaodian order error: goods not found: %s", outerId );
                    return null;
                }

                BigDecimal orderItemPrice = new BigDecimal(XPath.selectText("orderItemPrice", orderItem));
                OrderItems uhuilaOrderItem  = uhuilaOrder.addOrderItem(
                        goods,
                        Integer.parseInt(XPath.selectText("orderItemNum", orderItem)),
                        goodReceiverMobile,
                        orderItemPrice,
                        orderItemPrice
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
