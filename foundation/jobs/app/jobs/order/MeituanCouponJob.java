package jobs.order;

import com.google.gson.JsonObject;
import com.sun.java.util.jar.pack.Instruction;
import models.accounts.PaymentSource;
import models.admin.SupplierUser;
import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.order.*;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.MaterialType;
import models.sales.Shop;
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
            c.save();
            //默认是野生动物园的
            Shop shop = Shop.findById(3580L);
            //速8酒店
            if ("3538".equals(c.goods.id.toString())) {
                shop = Shop.findById(3941L);
            } else if ("3535".equals(c.goods.id.toString())) {
                //a8酒店
                shop = Shop.findById(3938L);
            }
            c.refresh();
            SupplierUser supplierUser = SupplierUser.find("byShop", shop).first();
            c.consumeAndPayCommission(shop.id, supplierUser, VerifyCouponType.AUTO_VERIFY);
            c.partner = ECouponPartner.MT;
            c.save();

            ECouponHistoryMessage.with(c).remark("系统定时自动生成美团订单,成功后自动验证")
                    .fromStatus(ECouponStatus.UNCONSUMED).toStatus(ECouponStatus.CONSUMED).sendToMQ();
        }
        return ybqOrder;
    }
}
