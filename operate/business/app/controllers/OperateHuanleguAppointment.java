package controllers;

import models.accounts.PaymentSource;
import models.admin.SupplierUser;
import models.huanlegu.HuanleguMessage;
import models.huanlegu.HuanleguUtil;
import models.operator.OperateUser;
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

import javax.persistence.Query;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author likang
 *         Date: 13-6-26
 */
@With(OperateRbac.class)
public class OperateHuanleguAppointment extends Controller {
    static String[] outerPartneLoginNamess = Play.configuration.getProperty("huanlegu.outer_partners", "wowotuan,gaopeng").split(",");

    @ActiveNavigation("sight_appointment_huanlegu")
    public static void index() {
        List<Resaler> outerPartners = new ArrayList<>();
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

    public static void appointmentWithOurOrder(String couponStr, String mobile, Date appointmentDate, String action, int count) {
        if (StringUtils.isBlank(couponStr)) {
            String err = "请填写券号";
            render("OperateHuanleguAppointment/withOurOrder.html", err, couponStr, mobile, appointmentDate);
        }

        //找到券信息
        ECoupon coupon = ECoupon.find("byECouponSn", couponStr).first();
        if (coupon == null) {
            String err = "券号不存在";
            render("OperateHuanleguAppointment/withOurOrder.html", err, couponStr, mobile, appointmentDate);
        }

        Supplier supplier = Supplier.findByDomainName(HuanleguUtil.SUPPLIER_DOMAIN_NAME);
        if (!coupon.goods.supplierId.equals(supplier.id)) {
            String err = "此券并非 欢乐谷/玛雅水世界 的券";
            render("OperateHuanleguAppointment/withOurOrder.html", err, couponStr, mobile, appointmentDate);
        }

        List<ECoupon> couponList = ECoupon.find("byOrder", coupon.order).fetch();

        if (count <= 0 || count >5 ) {
            count = 5;
        }
        //一次最多取count个
        List<ECoupon> tobeUsedCouponList = new ArrayList<>();
        for (ECoupon c : couponList) {
            if (c.status == ECouponStatus.UNCONSUMED) {
                tobeUsedCouponList.add(c);
                if (tobeUsedCouponList.size() >= count ) {
                    break;
                }
            }
        }
        if (tobeUsedCouponList.size() == 0) {
            String err = "无可用的券";
            render("OperateHuanleguAppointment/withOurOrder.html", err, couponStr, mobile, appointmentDate);
        }


        //处理查询
        if (StringUtils.isBlank(action)) {
            String err = "无效的操作";
            render("OperateHuanleguAppointment/withOurOrder.html", err, couponStr, mobile, appointmentDate);
        } else {
            if (action.equals("query")) {
                if (mobile == null) {
                    mobile = coupon.orderItems.phone;
                }
                //查看
                render("OperateHuanleguAppointment/withOurOrder.html", couponList, couponStr, mobile, appointmentDate);
            } else if (!action.equals("appointment")) {
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
            render("OperateHuanleguAppointment/withOurOrder.html", err, couponStr, appointmentDate, mobile);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(appointmentDate);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if ("SH1991007001441".equals(coupon.goods.supplierGoodsNo)){
            //周末
            if (dayOfWeek != 1 && dayOfWeek != 7) {
                String err = "周末券，但所选日期并非周末";
                render("OperateHuanleguAppointment/withOurOrder.html", err, couponStr, appointmentDate, mobile);
            }
        }else if ("SH1991007001444".equals(coupon.goods.supplierGoodsNo)) {
            //非周末
            if (dayOfWeek == 1 || dayOfWeek == 7) {
                String err = "非周末券，但所选日期是周末";
                render("OperateHuanleguAppointment/withOurOrder.html", err, couponStr, appointmentDate, mobile);
            }
        }

        //判断能不能买
        /*
        HuanleguMessage message = HuanleguUtil.checkTicketBuy(mobile, tobeUsedCouponList.size(), coupon.goods.supplierGoodsNo,
                coupon.goods.salePrice, appointmentDate);
        if (!message.isResponseOk()) {
            String err = message.errorMsg;
            render("OperateHuanleguAppointment/withOurOrder.html", err, couponStr, appointmentDate, mobile);
        } else {
            String isAllowBuy = message.selectTextTrim("./IsAllowBuy");
            if ("0".equals(isAllowBuy)) {
                String err = "无法购买：" + message.selectTextTrim("./Message");
                render("OperateHuanleguAppointment/withOurOrder.html", err, couponStr, appointmentDate, mobile);
            }
        }
        */

        //生成欢乐谷订单
        String errorMsg = confirmOrderOnHuanlegu(tobeUsedCouponList, mobile, appointmentDate);

        if (errorMsg == null) {
            StringBuilder usedCoupon = new StringBuilder();
            for (ECoupon c : tobeUsedCouponList) {
                usedCoupon.append(c.eCouponSn).append(",");
            }
            String success = "成功，请提醒用户注意查收短信/彩信。被使用的券号：" + usedCoupon.toString();
            render("OperateHuanleguAppointment/withOurOrder.html", success);
        } else {
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
        String[] inputCoupons = StringUtils.trimToEmpty(couponSn).split("\\s+");
        List<String> couponStrList = new ArrayList<>(inputCoupons.length);
        for (String coupon : inputCoupons) {
            coupon = StringUtils.trimToEmpty(coupon);
            if (StringUtils.isNotBlank(coupon)) {
                couponStrList.add(coupon);
            }
        }
        if (couponStrList.size() == 0 || couponStrList.size() > 5) {
            String err = "无有效的券,或券号数量大于5";
            render("OperateHuanleguAppointment/withoutOurOrder.html", err, goods, appointmentDate, mobile, couponSn, resaler, goodsList);
        }
        int len = -1;
        for(String c : couponStrList) {
            if (len == -1) {
                len = c.length();
            } else if(len != c.length()) {
                String err = "券号长度不一致，请检查";
                render("OperateHuanleguAppointment/withoutOurOrder.html", err, goods, appointmentDate, mobile, couponSn, resaler, goodsList);
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(appointmentDate);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if ("SH1991007001441".equals(goods.supplierGoodsNo)){
            if (dayOfWeek != 1 && dayOfWeek != 7) {
                String err = "周末券，但所选日期并非周末";
                render("OperateHuanleguAppointment/withoutOurOrder.html", err, goods, appointmentDate, mobile, couponSn, resaler, goodsList);
            }
        }else if ("SH1991007001444".equals(goods.supplierGoodsNo)) {
            //非周末
            if (dayOfWeek == 1 || dayOfWeek == 7) {
                String err = "非周末券，但所选日期是周末";
                render("OperateHuanleguAppointment/withoutOurOrder.html", err, goods, appointmentDate, mobile, couponSn, resaler, goodsList);
            }
        }

        Query query = JPA.em().createQuery("select count(c) from ECoupon c where c.order.userId = :userId and c.partnerCouponId in :couponList and status = :status");
        query.setParameter("userId", resaler.id);
        query.setParameter("couponList", couponStrList);
        query.setParameter("status", ECouponStatus.CONSUMED);

        long conflictCouponCount = (long)query.getSingleResult();
        if (conflictCouponCount > 0) {
            String err = "部分券已使用过，请检查";
            render("OperateHuanleguAppointment/withoutOurOrder.html", err, goods, appointmentDate, mobile, couponSn, resaler, goodsList);
        }


        //判断能不能买
        /*
        HuanleguMessage message = HuanleguUtil.checkTicketBuy(mobile, (int) couponStrList.size(), goods.supplierGoodsNo,
                goods.salePrice, appointmentDate);
        if (!message.isResponseOk()) {
            String err = message.errorMsg;
            render("OperateHuanleguAppointment/withoutOurOrder.html", err, goods, appointmentDate, mobile, couponSn, resaler, goodsList);
        } else {
            String isAllowBuy = message.selectTextTrim("./IsAllowBuy");
            if ("0".equals(isAllowBuy)) {
                String err = "无法购买：" + message.selectTextTrim("./Message");
                render("OperateHuanleguAppointment/withoutOurOrder.html", err, goods, appointmentDate, mobile, couponSn, resaler, goodsList);
            }
        }
        */

        //生成一百券订单
        Order ybqOrder = createYbqOrder(resaler, goods, couponStrList, mobile, goods.salePrice, OperateRbac.currentUser());

        List<ECoupon> couponList = ECoupon.findByOrder(ybqOrder);
        //生成欢乐谷订单
        String errorMsg = confirmOrderOnHuanlegu(couponList, mobile, appointmentDate);

        if (errorMsg == null) {
            String success = "成功，请提醒用户注意查收短信/彩信;已消费券号：" + StringUtils.join(couponStrList, ",");
            render("OperateHuanleguAppointment/withoutOurOrder.html", success);
        } else {
            JPA.em().getTransaction().setRollbackOnly();
            String err = errorMsg;
            render("OperateHuanleguAppointment/withoutOurOrder.html", err, goods, appointmentDate, mobile, couponSn, resaler, goodsList);
        }
    }

    @ActiveNavigation("sight_appointment_huanlegu")
    public static void appointmentWithoutOurOrderV2(Goods goods, Date appointmentDate, String mobile, String couponSn, Resaler resaler) {
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
        String[] inputCoupons = StringUtils.trimToEmpty(couponSn).split("\\s+");
        List<String> couponStrList = new ArrayList<>(inputCoupons.length);
        for (String coupon : inputCoupons) {
            coupon = StringUtils.trimToEmpty(coupon);
            if (StringUtils.isNotBlank(coupon)) {
                couponStrList.add(coupon);
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(appointmentDate);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if ("SH1991007001441".equals(goods.supplierGoodsNo)){
            if (dayOfWeek != 1 && dayOfWeek != 7) {
                String err = "周末券，但所选日期并非周末";
                render("OperateHuanleguAppointment/withoutOurOrder.html", err, goods, appointmentDate, mobile, couponSn, resaler, goodsList);
            }
        }else if ("SH1991007001444".equals(goods.supplierGoodsNo)) {
            //非周末
            if (dayOfWeek == 1 || dayOfWeek == 7) {
                String err = "非周末券，但所选日期是周末";
                render("OperateHuanleguAppointment/withoutOurOrder.html", err, goods, appointmentDate, mobile, couponSn, resaler, goodsList);
            }
        }

        Query query = JPA.em().createQuery("select count(c) from ECoupon c where c.order.userId = :userId and c.partnerCouponId in :couponList and status = :status");
        query.setParameter("userId", resaler.id);
        query.setParameter("couponList", couponStrList);
        query.setParameter("status", ECouponStatus.CONSUMED);

        long conflictCouponCount = (long)query.getSingleResult();
        if (conflictCouponCount > 0) {
            String err = "部分券已使用过，请检查";
            render("OperateHuanleguAppointment/withoutOurOrder.html", err, goods, appointmentDate, mobile, couponSn, resaler, goodsList);
        }

        //生成一百券订单
        Order ybqOrder = createYbqOrder(resaler, goods, couponStrList, mobile, goods.salePrice, OperateRbac.currentUser());

        List<ECoupon> couponList = ECoupon.findByOrder(ybqOrder);
        Shop shop = Shop.findShopBySupplier(supplier.id).get(0);
        SupplierUser supplierUser = SupplierUser.findBySupplier(supplier.id).get(0);
        for (ECoupon c : couponList) {
            if(!c.consumeAndPayCommission(shop.id, supplierUser, VerifyCouponType.AUTO_VERIFY)){
                JPA.em().getTransaction().setRollbackOnly();
            }
        }

        String success = "成功，请提醒用户注意查收短信/彩信;已消费券号：" + StringUtils.join(couponStrList, ",");
        render("OperateHuanleguAppointment/withoutOurOrder.html", success);
    }

    private static String confirmOrderOnHuanlegu(List<ECoupon> couponList, String mobile, Date appointmentDate) {

        ECoupon coupon = couponList.get(0);

        String orderNumber = coupon.order.orderNumber
                + "-" + new SimpleDateFormat("MMdd").format(new Date())
                + "-" + mobile.substring(mobile.length() - 4);

        Supplier supplier = Supplier.findByDomainName(HuanleguUtil.SUPPLIER_DOMAIN_NAME);
        Shop shop = Shop.findShopBySupplier(supplier.id).get(0);
        SupplierUser supplierUser = SupplierUser.findBySupplier(supplier.id).get(0);


        for (ECoupon c : couponList) {
            if(!c.consumeAndPayCommission(shop.id, supplierUser, VerifyCouponType.AUTO_VERIFY)){
                JPA.em().getTransaction().setRollbackOnly();
                return "券消费失败";
            }
        }

        HuanleguMessage message = HuanleguUtil.confirmOrder(orderNumber, coupon.createdAt, mobile,
                couponList.size(), coupon.goods.supplierGoodsNo, coupon.salePrice, appointmentDate);

        if (!message.isResponseOk()) {
            JPA.em().getTransaction().setRollbackOnly();
            return message.errorMsg;
        }

        Node voucher = message.selectNode("./Voucher");
        String supplierECouponId = StringUtils.trimToNull(XPath.selectText("./VoucherId", voucher));
        String supplierECouponPwd = StringUtils.trimToNull(XPath.selectText("./VoucherValue", voucher));

        String hvOrderId = message.selectTextTrim("./HvOrderId");

        for (ECoupon c : couponList) {
            ECouponHistoryMessage.with(c).remark("玛雅水世界预约成功后自动验证")
                    .fromStatus(ECouponStatus.UNCONSUMED).toStatus(ECouponStatus.CONSUMED).sendToMQ();
            c.supplierECouponPwd = supplierECouponPwd;
            c.supplierECouponId = supplierECouponId;
            c.extra = orderNumber + ";" + mobile.trim() + ";" + hvOrderId + ";" + couponList.size();
            c.save();
        }

        return null;
    }

    private static Order createYbqOrder(Resaler resaler, Goods goods, List<String> couponStrList,
                                        String mobile, BigDecimal salePrice, OperateUser operateUser) {
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
            ECouponHistoryMessage.with(c).remark("人工发券：" + operateUser.userName)
                    .fromStatus(ECouponStatus.UNCONSUMED).toStatus(ECouponStatus.UNCONSUMED).sendToMQ();
        }

        return ybqOrder;

    }
    @ActiveNavigation("sight_appointment_huanlegu_refund")
    public static void showRefund() {
        render();
    }

    @ActiveNavigation("sight_appointment_huanlegu_refund")
    public static void preRefund(String mobile, String supplierCoupon) {
        List<ECoupon> couponList;
        if (StringUtils.isNotBlank(mobile)) {
            Supplier supplier = Supplier.findByDomainName(HuanleguUtil.SUPPLIER_DOMAIN_NAME);
            List<Goods> goodsList = Goods.findBySupplierId(supplier.id);
            Query query = JPA.em().createQuery("select c from ECoupon c where c.orderItems.phone = :phone and c.orderItems.goods in :goodsList and c.extra is not null");
            query.setParameter("phone", mobile);
            query.setParameter("goodsList", goodsList);
            couponList = query.getResultList();
        }else if (StringUtils.isNotBlank(supplierCoupon)) {
            couponList = ECoupon.find("bySupplierECouponPwd", supplierCoupon).fetch();
        }else {
            error("请输入信息");
            return;
        }

        Map<String, List<ECoupon>> orderInfo = new HashMap<>();

        for (ECoupon coupon : couponList) {
            List<ECoupon> coupons = orderInfo.get(coupon.extra);
            if (coupons == null) {
                coupons = new ArrayList<>();
                orderInfo.put(coupon.extra, coupons);
            }
            coupons.add(coupon);
        }
        render(orderInfo);
    }

    @ActiveNavigation("sight_appointment_huanlegu_refund")
    public static void refund(String orderId, String hvOrderId, String supplierCouponValue, int count) {
        List<ECoupon> couponList = ECoupon.find("bySupplierECouponPwd", supplierCouponValue).fetch();
        if (couponList.size() != count) {
            error("订单内的券数量不一致");
        }

        boolean allRefunded = true;
        for (ECoupon coupon : couponList) {
            if (coupon.status != ECouponStatus.REFUND) {
                allRefunded = false;
            }
        }
        if (!allRefunded) {
            error("该订单有未退款的券，请首先做已消费退款。");
        }

        HuanleguMessage huanleguMessage = HuanleguUtil.orderRefund(orderId, hvOrderId, supplierCouponValue, count);

        String message = "取消预约成功";
        if (!huanleguMessage.isResponseOk()) {
            message = huanleguMessage.errorMsg;
        }
        render("OperateHuanleguAppointment/refundResult.html", message);
    }

    @ActiveNavigation("sight_appointment_huanlegu_refund")
    public static void resend(ECoupon coupon) {
        HuanleguMessage huanleguMessage = HuanleguUtil.resend(coupon);

        String message = "重发成功";
        if (!huanleguMessage.isResponseOk()) {
            message = huanleguMessage.errorMsg;
        }
        render("OperateHuanleguAppointment/refundResult.html", message);
    }
}
