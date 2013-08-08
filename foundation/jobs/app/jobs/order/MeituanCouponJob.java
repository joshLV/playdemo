package jobs.order;

import com.google.gson.JsonObject;
import models.accounts.PaymentSource;
import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.order.DeliveryType;
import models.order.ECoupon;
import models.order.ECouponHistoryMessage;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.MaterialType;
import play.Logger;
import play.jobs.Every;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * User: wangjia
 * Date: 13-7-24
 * Time: 上午9:43
 */
@JobDefine(title = "美团券生成", description = "处理OuterOrder中未生成的订单，生成券不发送")
@Every("1mn")
public class MeituanCouponJob extends JobWithHistory {

    @Override
    public void doJobWithHistory() {
        Logger.info("start meituan coupon job");
        List<OuterOrder> outerOrders = OuterOrder.find("partner = ? and  status = ?",
                OuterOrderPartner.MT,
                OuterOrderStatus.ORDER_COPY).fetch();
        for (OuterOrder outerOrder : outerOrders) {
            JsonObject jsonObject = outerOrder.getMessageAsJsonObject();
            Long goodsId = jsonObject.get("goodsId").getAsLong();
            Goods goods = Goods.findById(goodsId);
            List<String> couponStrList = new ArrayList<>();
            couponStrList.add(outerOrder.orderId);
            //生成一百券订单
            Order order = createYbqOrder(outerOrder.resaler, goods, couponStrList, null,
                    goods.salePrice);
            outerOrder.ybqOrder = order;
            outerOrder.status = OuterOrderStatus.ORDER_SYNCED;
            outerOrder.save();
            Logger.info("create maituan order successfully, orderId = ", order.id);
        }
    }

    private static Order createYbqOrder(Resaler resaler, Goods goods, List<String> couponStrList,
                                        String mobile, BigDecimal salePrice) {
        Order ybqOrder = Order.createResaleOrder(resaler);
        ybqOrder.save();

        OrderItems uhuilaOrderItem = ybqOrder.addOrderItem(goods, (long) couponStrList.size(), mobile, salePrice, salePrice);
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

        List<ECoupon> couponList = ECoupon.find("byOrder", ybqOrder).fetch();
        for (int i = 0; i < couponStrList.size(); i++) {
            ECoupon c = couponList.get(i);
            c.partnerCouponId = couponStrList.get(i);
            c.status = ECouponStatus.CONSUMED;
            c.save();
            ECouponHistoryMessage.with(c).remark("系统定时自动生成美团订单,成功后自动验证")
                    .fromStatus(ECouponStatus.UNCONSUMED).toStatus(ECouponStatus.CONSUMED).sendToMQ();
        }
        return ybqOrder;
    }
}
