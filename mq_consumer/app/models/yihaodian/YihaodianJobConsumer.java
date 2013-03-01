package models.yihaodian;

import models.RabbitMQConsumerWithTx;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.order.*;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.MaterialType;
import models.sales.ResalerProduct;
import org.w3c.dom.Node;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.jobs.OnApplicationStart;
import play.libs.XML;
import play.libs.XPath;

import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 */
@OnApplicationStart(async = true)
public class YihaodianJobConsumer extends RabbitMQConsumerWithTx<String> {
    public static String YHD_LOGIN_NAME = Resaler.YHD_LOGIN_NAME;
    public static String DELIVERY_SUPPLIER = Play.configuration.getProperty("yihaodian.delivery_supplier");

    @Override
    protected int retries() {
        return 0;//抛异常不重试
    }

    @Override
    public void consumeWithTx(String orderId) {
        //开启事务管理
        OuterOrder outerOrder = OuterOrder.find("byOrderIdAndPartner", orderId, OuterOrderPartner.YHD).first();
        if(outerOrder == null){
            Logger.info("yihaodidan job consume failed: can not find orderId " + orderId);
            return;
        }

        try{
            OuterOrder.em().refresh(outerOrder, LockModeType.PESSIMISTIC_WRITE);
        }catch (PersistenceException e){
            //拿不到锁就放弃
            Logger.info("yihaodian job consume failed: can not lock yihaodian order %s", orderId);
            return;
        }

        if(outerOrder.status == OuterOrderStatus.ORDER_DONE){
            if(syncWithYihaodian(orderId)){
                outerOrder.status = OuterOrderStatus.ORDER_SYNCED;
                outerOrder.save();
            }
        }else if(outerOrder.status == OuterOrderStatus.ORDER_COPY){
            //等 1 分钟再发货
            if(outerOrder.createdAt.getTime() >= (System.currentTimeMillis() - 60000)){
                return;
            }

            //首先刷新最新的订单信息
            Map<String, String> params = new HashMap<>();
            params.put("orderCode", orderId);
            YHDResponse response = YHDUtil.sendRequest(params, "yhd.order.detail.get", "orderInfo");
            if(!response.isOk()){
                return;
            }
            outerOrder.message = XML.serialize(response.data.getOwnerDocument());//保存订单信息

            //订单已取消的话不处理
            YHDOrderStatus orderStatus = YHDOrderStatus.valueOf(response.selectTextTrim("./orderDetail/orderStatus"));
            if (YHDOrderStatus.ORDER_CANCEL == orderStatus) {
                Logger.error("yihaodian job consume warning: order is canceled");
                outerOrder.status = OuterOrderStatus.ORDER_CANCELED;
                outerOrder.save();
                return;
            }

            //挑选出电子券的orderItem
            List<Node> orderItems = response.selectNodes("./orderItemList/orderItem");
            List<Node> couponItems = new ArrayList<>();
            for (Node orderItem : orderItems) {
                String outerId = XPath.selectText("./outerId", orderItem);
                if (outerId != null) {
                    outerId = outerId.trim();
                    Goods goods = ResalerProduct.getGoods(Long.parseLong(outerId), OuterOrderPartner.YHD);
                    if (goods != null && goods.materialType == MaterialType.ELECTRONIC) {
                        couponItems.add(orderItem);
                    }else {
                        Logger.error("yihaodian job consume warning: goods not found " + outerId);
                    }
                }
            }
            //如果没有电子券的订单项 忽略此订单
            if (couponItems.size() == 0) {
                outerOrder.status = OuterOrderStatus.ORDER_IGNORE;
                outerOrder.save();
                Logger.info("yihaodian job consume failed: no electronic goods");
                return;
            }

            //如果没有生成一百券的订单，生成一下
            if(outerOrder.ybqOrder == null) {
                String goodsReceiverMobile = response.selectTextTrim("./orderDetail/goodReceiverMoblie");
                outerOrder.ybqOrder = buildYibaiquanOrder(couponItems, goodsReceiverMobile);
            }
            if (outerOrder.ybqOrder == null) {
                Logger.error("yihaodian job consume failed: build our order failed");
                outerOrder.save();
                return;
            }else {
                outerOrder.status = OuterOrderStatus.ORDER_DONE;
            }
            outerOrder.save();

            if (couponItems.size() < orderItems.size()) {
                MailMessage mailMessage = new MailMessage();
                mailMessage.addRecipient("op@uhuila.com");
                mailMessage.setSubject("一号店实体券订单");
                mailMessage.putParam("yhdOrderId", orderId);
                mailMessage.setTemplate("yihaodianRealGoods");
                MailUtil.sendCommonMail(mailMessage);
            }

            //设置券为一号店生成的
            List<ECoupon> couponList = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
            for(ECoupon coupon : couponList) {
                coupon.partner = ECouponPartner.YHD;
                coupon.save();
            }
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

    private boolean checkYihaodianSent(String orderCode) {
        Map<String, String> params = new HashMap<>();
        params.put("orderCode", orderCode);

        YHDResponse response = YHDUtil.sendRequest(params, "yhd.order.detail.get", "orderInfo");
        if (response.isOk()) {
            YHDOrderStatus orderStatus = YHDOrderStatus.valueOf(response.selectTextTrim("./orderDetail/orderStatus"));
            if (orderStatus != YHDOrderStatus.ORDER_PAYED
                    && orderStatus != YHDOrderStatus.ORDER_WAIT_PAY
                    && orderStatus != YHDOrderStatus.ORDER_CANCEL) {
                //只要不是待付款、已付款或取消状态状态，就说明我们已经以某种渠道发过货了
                return true;
            }
        }
        return false;
    }

    private models.order.Order buildYibaiquanOrder(List<Node> couponItems, String goodReceiverMobile) {
        Resaler resaler = Resaler.findOneByLoginName(YHD_LOGIN_NAME);
        if (resaler == null){
            Logger.error("can not find the resaler by login name: %s", YHD_LOGIN_NAME);
            return null;
        }
        models.order.Order uhuilaOrder = models.order.Order.createConsumeOrder(resaler.getId(), AccountType.RESALER);
        uhuilaOrder.save();
        try {
            for (Node orderItem : couponItems){
                String outerId = XPath.selectText("./outerId", orderItem).trim();
                Goods goods = ResalerProduct.getGoods(Long.parseLong(outerId), OuterOrderPartner.YHD);

                BigDecimal orderItemPrice = new BigDecimal(XPath.selectText("./orderItemPrice", orderItem).trim());
                OrderItems uhuilaOrderItem  = uhuilaOrder.addOrderItem(
                        goods,
                        Integer.parseInt(XPath.selectText("./orderItemNum", orderItem).trim()),
                        goodReceiverMobile,
                        orderItemPrice,
                        orderItemPrice
                );
                uhuilaOrderItem.save();
            }
        } catch (NotEnoughInventoryException e) {
            Logger.info("yihaodian order build failed: inventory not enough");
            JPA.em().getTransaction().rollback();
            return null;
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
        return String.class;
    }

    @Override
    protected String queue() {
        return YihaodianQueueUtil.QUEUE_NAME;
    }
}
