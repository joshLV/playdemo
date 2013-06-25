package controllers;

import models.admin.SupplierUser;
import models.huanlegu.HuanleguMessage;
import models.huanlegu.HuanleguUtil;
import models.order.ECoupon;
import models.order.VerifyCouponType;
import models.sales.Shop;
import models.supplier.Supplier;
import play.Logger;
import play.mvc.Controller;

/**
 * @author likang
 *         Date: 13-6-25
 */
public class HuanleguAPI extends Controller {
    public static void couponConsumed(String xmlContent) {
        Logger.info("huanlegu coupon consumed: %s", xmlContent);
        HuanleguMessage message = HuanleguUtil.parseMessage(xmlContent, false);
        /**
         * <OrderId>分销商订单编号</OrderId>
         <HvOrderId>运营商平台的订单编号</HvOrderId>
         < VoucherValue >凭证值</VoucherValue>
         < ConsumeTimes>消费次数</ ConsumeTimes >
         < ConsumeDate>消费时间</ ConsumeDate >
         */
        Long orderId = Long.parseLong(message.selectTextTrim("./OrderId"));
        Long supplierOrderId = Long.parseLong(message.selectTextTrim("./HvOrderId"));
        String supplierCoupon = message.selectTextTrim("./VoucherValue");
        Long consumeTimes = Long.parseLong(message.selectTextTrim("./ConsumeTimes"));
        String consumeDate = message.selectTextTrim("./ConsumeDate");

        if (orderId == null || supplierCoupon == null) {

        }

        ECoupon coupon = ECoupon.find("order.id = ? and supplierECouponPwd = ?", orderId, supplierCoupon).first();
        if (coupon != null) {
            Supplier supplier = Supplier.findByDomainName(HuanleguUtil.SUPPLIER_DOMAIN_NAME);
            Shop shop = Shop.findShopBySupplier(supplier.id).get(0);
            SupplierUser supplierUser = SupplierUser.findBySupplier(supplier.id).get(0);
            coupon.consumeAndPayCommission(shop.id, supplierUser, VerifyCouponType.AUTO_VERIFY);
            Logger.info("huanlegu coupon consumed success");
        }

    }
}
