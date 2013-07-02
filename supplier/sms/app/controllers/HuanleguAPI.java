package controllers;

import models.huanlegu.HuanleguMessage;
import models.huanlegu.HuanleguUtil;
import models.order.ECoupon;
import play.Logger;
import play.mvc.Controller;

import java.util.List;

/**
 * @author likang
 *         Date: 13-6-25
 */
public class HuanleguAPI extends Controller {
    public static void couponConsumed(String xmlContent) {
        Logger.info("huanlegu coupon consumed: %s", xmlContent);
        HuanleguMessage message = HuanleguUtil.parseMessage(xmlContent, false);

        String orderNumberStr = message.selectTextTrim("./OrderId");
        String[] orderNumberInfos = orderNumberStr.split("-");
//        Long supplierOrderId = Long.parseLong(message.selectTextTrim("./HvOrderId"));
        String supplierCoupon = message.selectTextTrim("./VoucherValue");
        Long consumeTimes = Long.parseLong(message.selectTextTrim("./ConsumeTimes"));
//        String consumeDate = message.selectTextTrim("./ConsumeDate");

        List<ECoupon> couponList = ECoupon.find("order.orderNumber = ? and supplierECouponPwd = ?", orderNumberInfos[0], supplierCoupon).fetch();
        for (ECoupon coupon : couponList) {
            String tmp[] = coupon.extra.split(";");
            if (tmp.length == 4) {
                coupon.extra = coupon.extra + ";" + consumeTimes;
                coupon.save();
            }
        }
    }
}
