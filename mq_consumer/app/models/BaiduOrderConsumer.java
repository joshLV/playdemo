package models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import models.accounts.PaymentSource;
import models.baidu.BaiduOrderMessageUtil;
import models.baidu.BaiduResponse;
import models.baidu.BaiduUtil;
import models.mq.RabbitMQConsumerWithTx;
import models.order.*;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.MaterialType;
import models.sales.ResalerProduct;
import models.sina.SinaVouchersMessageUtil;
import play.Logger;
import play.db.jpa.JPA;
import play.jobs.OnApplicationStart;

import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: yan
 * Date: 13-7-12
 * Time: 下午9:19
 */
@OnApplicationStart(async = true)
public class BaiduOrderConsumer extends RabbitMQConsumerWithTx<String> {

    @Override
    public void consumeWithTx(String outOrderId) {
        //开启事务管理
        OuterOrder outerOrder = OuterOrder.find("byOrderIdAndPartner", outOrderId, OuterOrderPartner.BD).first();
        if (outerOrder == null) {
            Logger.info("baidu job consume failed: can not find orderId " + outOrderId);
            return;
        }

        try {
            OuterOrder.em().refresh(outerOrder, LockModeType.PESSIMISTIC_WRITE);
        } catch (PersistenceException e) {
            //拿不到锁就放弃
            Logger.info("baidu job consume failed: can not lock baidu order %s", outOrderId);
            return;
        }
        if (outerOrder.status == OuterOrderStatus.ORDER_COPY) {
            //订单接收到，开始创建一百券订单
            Logger.info("start baidu order consumer,orderId:%s", outOrderId);
            if (outerOrder.ybqOrder != null) {
                Logger.info("baidu our order already created");
            } else {
                //首先刷新下百度最新订单
                Map<String, Object> params = new HashMap<>();
                params.put("order_id", outOrderId);
                BaiduResponse response = BaiduUtil.sendRequest(params, "orderdetails.action");
                if (!response.isOk()) {
                    Logger.info("baidu order details not the lastest,orderId:%s", outOrderId);
                    return;
                }
                JsonObject responseData = response.data.getAsJsonObject();
                String status = responseData.get("status").getAsString();
                //如果百度订单不是已付款状态，则直接返回
                if (!"2".equals(status)) {
                    Logger.info("baidu order status is %s,can't create ybqOrder,orderId:%s", outOrderId);
                    return;
                }
                //开始创建一百券订单
                JsonObject jsonObject = outerOrder.getMessageAsJsonObject();
                String goodsLinkId = jsonObject.get("tpid").getAsString();
                JsonArray couponArr = jsonObject.get("coupon").getAsJsonArray();
                Long number = responseData.get("count").getAsLong();
                if (number != couponArr.size()) {
                    Logger.info("baidu order and coupon number is not equal ,orderId:%s", outOrderId);
                    return;
                }

                Order ybqOrder = createYbqOrder(goodsLinkId, responseData);
                if (ybqOrder != null) {
                    List<ECoupon> couponList = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
                    for (int i = 0; i < couponList.size(); i++) {
                        ECoupon coupon = couponList.get(i);
                        coupon.eCouponSn = couponArr.get(i).getAsJsonObject().get("code").getAsString();
                        coupon.partner = ECouponPartner.BD;
                        coupon.save();
                    }

                    outerOrder.ybqOrder = ybqOrder;
                    outerOrder.status = OuterOrderStatus.ORDER_SYNCED;
                    outerOrder.save();
                }

            }
        }
    }

    private Order createYbqOrder(String goodsLinkId, JsonObject responseData) {

        Resaler resaler = Resaler.findOneByLoginName(Resaler.BAIDU_LOGIN_NAME);
        if (resaler == null) {
            Logger.error("can not find the resaler by login name: %s", Resaler.BAIDU_LOGIN_NAME);
            return null;
        }
        Order ybqOrder = Order.createResaleOrder(resaler);
        ybqOrder.save();
        Goods goods = ResalerProduct.getGoods(resaler, Long.valueOf(goodsLinkId), OuterOrderPartner.TB);

        if (goods == null) {
            Logger.info("goods not found: %s", goodsLinkId);
            return null;
        }

        Long number = responseData.get("count").getAsLong();
        BigDecimal salePrice = responseData.get("amount").getAsBigDecimal();
        String userPhone = responseData.get("phone").getAsString();
        //导入券库存检查
        if (goods.hasEnoughInventory(number)) {
            Logger.error("enventory not enough: goods.id=" + goods.id);
            JPA.em().getTransaction().rollback();
            return null;
        }

        OrderItems uhuilaOrderItem = ybqOrder.addOrderItem(goods, number,
                userPhone, salePrice, salePrice);
        uhuilaOrderItem.save();

        if (goods.materialType.equals(MaterialType.REAL)) {
            ybqOrder.deliveryType = DeliveryType.LOGISTICS;
        } else if (goods.materialType.equals(MaterialType.ELECTRONIC)) {
            ybqOrder.deliveryType = DeliveryType.SMS;
        }

        ybqOrder.createAndUpdateInventory();
        ybqOrder.accountPay = ybqOrder.needPay;
        ybqOrder.discountPay = BigDecimal.ZERO;
        ybqOrder.payMethod = PaymentSource.getBalanceSource().code;
        ybqOrder.payAndSendECoupon();
        ybqOrder.save();
        return ybqOrder;

    }

    @Override
    protected Class getMessageType() {
        return String.class;
    }

    @Override
    protected String queue() {
        return BaiduOrderMessageUtil.QUEUE_NAME;
    }
}
