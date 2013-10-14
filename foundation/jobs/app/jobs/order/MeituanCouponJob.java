package jobs.order;

import com.google.gson.JsonObject;
import models.accounts.PaymentSource;
import models.admin.SupplierUser;
import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.order.*;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.MaterialType;
import models.sales.Shop;
import models.sales.SupplierResalerShop;
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
        List<OuterOrder> outerOrders = OuterOrder.find("(partner = ? or partner = ? or partner = ?) and  status = ?",
                OuterOrderPartner.MT,
                OuterOrderPartner.DP,
                OuterOrderPartner.NM,
                OuterOrderStatus.ORDER_COPY).fetch();
        for (OuterOrder outerOrder : outerOrders) {
            JsonObject jsonObject = outerOrder.getMessageAsJsonObject();
            Long goodsId = jsonObject.get("goodsId").getAsLong();
            Long shopId = jsonObject.get("shopId").getAsLong();
            Goods goods = Goods.findById(goodsId);
            List<String> couponStrList = new ArrayList<>();
            couponStrList.add(outerOrder.orderId);
            //生成一百券订单
            Order order = createYbqOrder(outerOrder, goods, shopId, couponStrList, null,
                    goods.salePrice);
            outerOrder.ybqOrder = order;
            outerOrder.status = OuterOrderStatus.ORDER_SYNCED;
            outerOrder.save();
            Logger.info("create maituan order successfully, orderId = ", order.id);
        }
    }

    private static Order createYbqOrder(OuterOrder outerOrder, Goods goods, Long shopId, List<String> couponStrList,
                                        String mobile, BigDecimal salePrice) {
        Order ybqOrder = Order.createResaleOrder(outerOrder.resaler);
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
            c.save();

            Shop shop = Shop.findById(shopId);
            c.refresh();
            SupplierUser supplierUser = SupplierUser.find("byShop", shop).first();
            c.consumeAndPayCommission(shop.id, supplierUser, VerifyCouponType.AUTO_VERIFY);
            c.partner = getECouponPartner(outerOrder.partner.toString());
            c.save();

            ECouponHistoryMessage.with(c).remark("系统定时自动生成" + outerOrder.partner.partnerName() + "订单,成功后自动验证")
                    .fromStatus(ECouponStatus.UNCONSUMED).toStatus(ECouponStatus.CONSUMED).sendToMQ();
        }
        return ybqOrder;
    }

    private static ECouponPartner getECouponPartner(String partner) {
        if ("DP".equals(partner)) {
            return ECouponPartner.DP;
        }
        if ("MT".equals(partner)) {
            return ECouponPartner.MT;
        }
        if ("NM".equals(partner)) {
            return ECouponPartner.NM;
        }
        return null;
    }

}
