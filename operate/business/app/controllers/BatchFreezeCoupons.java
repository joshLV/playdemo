package controllers;

import models.order.CheatedOrderSource;
import models.order.ECoupon;
import models.order.ECouponStatus;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 批量冻结券
 * <p/>
 * User: wangjia
 * Date: 12-10-23
 * Time: 上午11:31
 */
@With(OperateRbac.class)
@ActiveNavigation("freeze_coupons")
public class BatchFreezeCoupons extends Controller {

    @ActiveNavigation("freeze_coupons")
    public static void index(String couponsFreezed) {
        render(couponsFreezed);
    }

    public static void importFreezeCoupons(String couponsFreezed) {
        if (StringUtils.isBlank(couponsFreezed))
            Validation.addError("couponsFreezed", "validation.required");

        if (Validation.hasErrors()) {
            render("OperateCoupons/createCouponsFreezed.html", couponsFreezed);
        }

        Pattern p = Pattern.compile("[,\\s]+");
        String[] couponSns = p.split(couponsFreezed);
        List<String> inExistentCoupons = new ArrayList<>();
        List<ECoupon> usedCouponsList = new ArrayList<>();
        List<ECoupon> freezedCouponsList = new ArrayList<>();
        List<ECoupon> supplierCheatedCouponsList = new ArrayList<>();
        Set<ECoupon> unUsedCouponsList = new HashSet<>();
        BigDecimal tempUnUsed = BigDecimal.ZERO;
        BigDecimal tempFreezed = BigDecimal.ZERO;
        BigDecimal sumUnUsed = BigDecimal.ZERO;
        BigDecimal sumFreezed = BigDecimal.ZERO;
        BigDecimal sumSupplierCheated = BigDecimal.ZERO;
        String couponsFreezedId = "";
        for (int i = 0; i < couponSns.length; i++) {
            //不存在的券号
            ECoupon tempCoupon = ECoupon.find("eCouponSn=?", couponSns[i]).first();
            if (tempCoupon == null) {
                inExistentCoupons.add(couponSns[i]);
                continue;
            }
            //已使用的券号
            tempCoupon = ECoupon.find("eCouponSn=? and status=?", couponSns[i], ECouponStatus.CONSUMED).first();
            if (tempCoupon != null) {
                usedCouponsList.add(tempCoupon);
                continue;
            }
            //已冻结的券号
            tempCoupon = ECoupon.find("eCouponSn=? and isFreeze=?", couponSns[i], 1).first();
            if (tempCoupon != null) {
                freezedCouponsList.add(tempCoupon);
                sumFreezed = sumFreezed.add(tempFreezed.add(tempCoupon.salePrice));
                continue;
            }
            //商户已刷单的券号
            tempCoupon = ECoupon.find("eCouponSn=? and isCheatedOrder=? and cheatedOrderSource = ?", couponSns[i], Boolean.TRUE, CheatedOrderSource.SUPPLIER).first();
            if (tempCoupon != null) {
                supplierCheatedCouponsList.add(tempCoupon);
                sumSupplierCheated = sumSupplierCheated.add(tempFreezed.add(tempCoupon.salePrice));
                continue;
            }

            //未消费的券号
            tempCoupon = ECoupon.find("eCouponSn=? and status=?", couponSns[i], ECouponStatus.UNCONSUMED).first();
            if (tempCoupon != null) {
                unUsedCouponsList.add(tempCoupon);
                sumUnUsed = sumUnUsed.add(tempUnUsed.add(tempCoupon.salePrice));
                continue;
            }

        }
        Iterator it = unUsedCouponsList.iterator();
        while (it.hasNext()) {
            ECoupon temp = (ECoupon) it.next();
            couponsFreezedId += temp.id + ",";
        }
        render(inExistentCoupons, usedCouponsList, freezedCouponsList, unUsedCouponsList, sumUnUsed, sumFreezed, couponsFreezedId,supplierCheatedCouponsList,sumSupplierCheated);
    }

    public static void batchFreezeCoupons(String couponsFreezedId,String commissionRatio, ECoupon coupon) {
        Set<ECoupon> unUsedCouponsList = new HashSet<>();
        BigDecimal sumUnUsed = BigDecimal.ZERO;
        BigDecimal tempUnUsed = BigDecimal.ZERO;
        String c[] = couponsFreezedId.split(",");
        for (int i = 0; i < c.length; i++) {
            ECoupon tempCoupon = ECoupon.findById(Long.parseLong(c[i]));
            unUsedCouponsList.add(tempCoupon);
            sumUnUsed = sumUnUsed.add(tempUnUsed.add(tempCoupon.salePrice));
            ECoupon.freeze(Long.parseLong(c[i]), OperateRbac.currentUser().userName, coupon,commissionRatio);
        }
        render(unUsedCouponsList, sumUnUsed);
    }
}
