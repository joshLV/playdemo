package controllers;

import models.accounts.PaymentSource;
import models.admin.SupplierUser;
import models.huanlegu.HuanleguMessage;
import models.huanlegu.HuanleguUtil;
import models.order.*;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.MaterialType;
import models.sales.Shop;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.w3c.dom.Node;
import play.Play;
import play.db.jpa.JPA;
import play.libs.XPath;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author likang
 *         Date: 13-6-26
 */
@With(OperateRbac.class)
public class OperateHuanleguAppointment extends Controller {
    static String[] outerPartneLoginNamess = Play.configuration.getProperty("huanlegu.outer_partners", "wowotuan,gaopeng").split(",");

    @ActiveNavigation("sight_appointment_huanlegu")
    public static void index() {
        List<Resaler> outerPartners = new ArrayList();
        for (String outerPartner : outerPartneLoginNamess) {
            Resaler resaler = Resaler.findOneByLoginName(outerPartner);
            if (resaler != null) {
                outerPartners.add(resaler);
            }
        }

        Supplier supplier = Supplier.findByDomainName(HuanleguUtil.SUPPLIER_DOMAIN_NAME);
        List<Goods> goodsList = Goods.findBySupplierId(supplier.id);
        render(goodsList, outerPartners);
    }

    @ActiveNavigation("sight_appointment_huanlegu")
    public static void withOurOrder() {
        render();
    }

    public static void appointmentWithOurOrder(String couponStr, String mobile, Date appointmentDate, String action) {
        if (StringUtils.isBlank(couponStr)) {
            String err = "请填写券号";
            render("OperateHuanleguAppointment/withOurOrder.html", err, couponStr, mobile, appointmentDate);
        }

        //找到券信息
        ECoupon coupon = ECoupon.find("byECouponSn", couponStr).first();
        if (coupon == null) {
            String err = "券号不存在";
            render("OperateHuanleguAppointment/withOurOrder.html", err, couponStr, mobile, appointmentDate);
        }else if (coupon.status != ECouponStatus.UNCONSUMED) {
            String err = "券号不是未消费状态";
            render("OperateHuanleguAppointment/withOurOrder.html", err, couponStr, mobile, appointmentDate);
        }
        List<ECoupon> couponList = coupon.orderItems.getECoupons();

        Supplier supplier = Supplier.findByDomainName(HuanleguUtil.SUPPLIER_DOMAIN_NAME);
        if (!coupon.goods.supplierId.equals(supplier.id)) {
            String err = "此券并非 欢乐谷/玛雅水世界 的券";
            render("OperateHuanleguAppointment/withOurOrder.html", err, couponStr, mobile, appointmentDate);
        }

        //处理查询
        if (StringUtils.isBlank(action) ){
            String err = "无效的操作";
            render("OperateHuanleguAppointment/withOurOrder.html", err, couponStr, mobile, appointmentDate);
        }else {
            if (action.equals("query")) {
                if (mobile == null) {
                    mobile = coupon.orderItems.phone;
                }
                //查看
                render("OperateHuanleguAppointment/withOurOrder.html", couponList, couponStr, mobile, appointmentDate);
            }else if (!action.equals("appointment")) {
                String err = "无效的操作";
                render("OperateHuanleguAppointment/withOurOrder.html", err, couponStr, mobile, appointmentDate);
            }
        }

        //处理预约

        if (StringUtils.isBlank(mobile)) {
            String err = "请填写手机号";
            render("OperateHuanleguAppointment/withOurOrder.html", err, couponStr, mobile, appointmentDate);
        }
        if (appointmentDate == null || appointmentDate.before(DateUtils.ceiling(new Date(), Calendar.DATE))
                || appointmentDate.after(DateUtils.ceiling(DateUtils.addDays(new Date(), 30), Calendar.DATE))) {
            String err = "预约日期从明天开始，30天内";
            render("OperateHuanleguAppointment/withoutOurOrder.html", err, couponStr, appointmentDate, mobile);
        }

        int quantity = couponList.size() > 5 ? 5 : couponList.size();

        //生成欢乐谷订单
        String errorMsg = confirmOrderOnHuanlegu(quantity, couponList, mobile, appointmentDate);

        if (errorMsg == null) {
            String success = "成功，请提醒用户注意查收短信/彩信";
            render("OperateHuanleguAppointment/withOurOrder.html", success);
        }else {
            String err = errorMsg;
            render("OperateHuanleguAppointment/withOurOrder.html", err, couponStr, mobile, appointmentDate);
        }
    }

    @ActiveNavigation("sight_appointment_huanlegu")
    public static void withoutOurOrder(Long resalerId) {
        Supplier supplier = Supplier.findByDomainName(HuanleguUtil.SUPPLIER_DOMAIN_NAME);
        List<Goods> goodsList = Goods.findBySupplierId(supplier.id);

        Resaler resaler = Resaler.findById(resalerId);

        render(goodsList, resaler);
    }

    @ActiveNavigation("sight_appointment_huanlegu")
    public static void appointmentWithoutOurOrder(Goods goods, Date appointmentDate, String mobile, String couponSn, Resaler resaler) {
        Supplier supplier = Supplier.findByDomainName(HuanleguUtil.SUPPLIER_DOMAIN_NAME);
        List<Goods> goodsList = Goods.findBySupplierId(supplier.id);
        if (goods == null || !goods.supplierId.equals(supplier.id)) {
            String err = "无效的商品";
            render("OperateHuanleguAppointment/withoutOurOrder.html", err, goods, appointmentDate, mobile, couponSn, resaler, goodsList);
        }
        if (resaler == null) {
            String err = "无效的分销商";
            render("OperateHuanleguAppointment/withoutOurOrder.html", err, goods, appointmentDate, mobile, couponSn, resaler, goodsList);
        }
        if (appointmentDate == null || appointmentDate.before(DateUtils.ceiling(new Date(), Calendar.DATE))
                || appointmentDate.after(DateUtils.ceiling(DateUtils.addDays(new Date(), 30), Calendar.DATE))) {
            String err = "预约日期从明天开始，30天内";
            render("OperateHuanleguAppointment/withoutOurOrder.html", err, goods, appointmentDate, mobile, couponSn, resaler, goodsList);
        }
        String[] inputCoupons = StringUtils.trimToEmpty(couponSn).split("\\r?\\n");
        List<String> coupons = new ArrayList<>(inputCoupons.length);
        for(String coupon : inputCoupons) {
            coupon = StringUtils.trimToEmpty(coupon);
            if (StringUtils.isNotBlank(coupon)) {
                coupons.add(coupon);
            }
        }
        if (coupons.size() == 0 || coupons.size() > 5) {
            String err = "无有效的券,或券号数量大于5";
            render("OperateHuanleguAppointment/withoutOurOrder.html", err, goods, appointmentDate, mobile, couponSn, resaler, goodsList);
        }
        //生成一百券订单
        Order ybqOrder = createYbqOrder(resaler, goods, (long)coupons.size(), mobile, goods.salePrice);

        List<ECoupon> couponList = ECoupon.findByOrder(ybqOrder);
        //生成欢乐谷订单
        String errorMsg = confirmOrderOnHuanlegu(couponList.size(), couponList, mobile, appointmentDate);

        if (errorMsg == null) {
            String success = "成功，请提醒用户注意查收短信/彩信";
            render("OperateHuanleguAppointment/withoutOurOrder.html", success);
        }else {
            JPA.em().getTransaction().rollback();
            String err = errorMsg;
            render("OperateHuanleguAppointment/withoutOurOrder.html", err, goods, appointmentDate, mobile, couponSn, resaler, goodsList);
        }
    }

    private static String confirmOrderOnHuanlegu(int quantity, List<ECoupon> couponList, String mobile, Date appointmentDate) {
        ECoupon coupon = couponList.get(0);

        HuanleguMessage message = HuanleguUtil.confirmOrder(coupon.order.orderNumber, coupon.createdAt, mobile,
                quantity, coupon.goods.supplierGoodsNo, coupon.salePrice, appointmentDate);

        if (!message.isResponseOk()) {
            return message.errorMsg;
        }

        Node voucher = message.selectNode("./Voucher");
        String supplierECouponPwd = StringUtils.trimToNull(XPath.selectText("./VoucherValue", voucher));
        String supplierECouponId = StringUtils.trimToNull(XPath.selectText("./VoucherId", voucher));//password字段存成 景点门票的ID

        Date today = DateUtils.truncate(new Date(), Calendar.DATE);
        Date tomorrow = DateUtils.ceiling(new Date(), Calendar.DATE);

        Supplier supplier = Supplier.findByDomainName(HuanleguUtil.SUPPLIER_DOMAIN_NAME);
        Shop shop = Shop.findShopBySupplier(supplier.id).get(0);
        SupplierUser supplierUser = SupplierUser.findBySupplier(supplier.id).get(0);

        for (int i = 0; i < quantity; i++) {
            ECoupon c = couponList.get(i);
            c.supplierECouponPwd = supplierECouponPwd;
            c.supplierECouponId = supplierECouponId;
            c.effectiveAt = today;
            c.expireAt = tomorrow;
            c.save();
            c.consumeAndPayCommission(shop.id, supplierUser, VerifyCouponType.AUTO_VERIFY);
        }

        coupon.order.supplierOrderNumber = message.selectTextTrim("./HvOrderId");
        coupon.order.save();

        return null;
    }

    private static Order createYbqOrder(Resaler resaler, Goods goods, Long count, String mobile, BigDecimal salePrice) {
        Order ybqOrder = Order.createResaleOrder(resaler);
        ybqOrder.save();

        OrderItems uhuilaOrderItem = ybqOrder.addOrderItem(goods, count, mobile, salePrice, salePrice);
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
}
