package controllers;

import models.admin.OperateUser;
import models.order.*;
import models.sales.Brand;
import operate.rbac.ContextedPermission;
import operate.rbac.annotations.ActiveNavigation;
import operate.rbac.annotations.Right;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

@With(OperateRbac.class)
@ActiveNavigation("coupons_index")
public class OperateCoupons extends Controller {

    public static int PAGE_SIZE = 15;

    /**
     * 券号列表
     */
    @ActiveNavigation("coupons_index")
    public static void index(CouponsCondition condition) {
        if (condition == null) {
            condition = new CouponsCondition();
        }
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        Boolean hasSeeAllSupplierPermission = ContextedPermission.hasPermission("SEE_ALL_SUPPLIER");
        Long operatorId = OperateRbac.currentUser().id;
        JPAExtPaginator<ECoupon> couponPage;
        if (!Play.runingInTestMode()) {
            couponPage = ECoupon.query(condition, pageNumber, PAGE_SIZE, operatorId, hasSeeAllSupplierPermission);
        } else {
            couponPage = ECoupon.query(condition, pageNumber, PAGE_SIZE, null, true);
        }
        for (ECoupon coupon : couponPage) {
            if (coupon.operateUserId != null) {
                OperateUser operateUser = OperateUser.findById(coupon.operateUserId);
                coupon.operateUserName = operateUser.userName;
            }
        }
        List<Brand> brandList = Brand.findByOrder(null, operatorId, hasSeeAllSupplierPermission);
        renderArgs.put("brandList", brandList);
        BigDecimal amountSummary = ECoupon.summary(couponPage);
        //判断角色是否有解冻券号的权限
        boolean hasRight = ContextedPermission.hasPermission("COUPON_UNFREEZE");
        render(couponPage, condition, amountSummary, hasRight);
    }

    /**
     * 冻结此券
     *
     * @param id
     */
    public static void freeze(long id) {
        ECoupon.freeze(id, OperateRbac.currentUser().userName);
        index(null);
    }

    /**
     * 解冻此券
     *
     * @param id
     */
    @Right("manager")
    public static void unfreeze(long id) {
        ECoupon.unfreeze(id, OperateRbac.currentUser().userName);
        index(null);
    }

    /**
     * 券号列表
     */
    public static void couponHistory(String couponSn) {
        ECoupon coupon = ECoupon.find("eCouponSn=?", couponSn).first();
        if (coupon == null) {
            return;
        }

        List<CouponHistory> couponList = CouponHistory.find("coupon=?", coupon).fetch();
        render("OperateCoupons/history.html", couponSn, couponList);
    }

    /**
     * 重发短信
     *
     * @param id
     */
    public static void sendMessage(long id) {
        boolean sendFalg = ECoupon.sendMessage(id);
        ECoupon eCoupon = ECoupon.findById(id);
        new CouponHistory(eCoupon, OperateRbac.currentUser().userName, "重发短信", eCoupon.status, eCoupon.status, null).save();
        renderJSON(sendFalg ? "0" : "1");
    }

    public static void couponExcelOut(CouponsCondition condition) {

        if (condition == null) {
            condition = new CouponsCondition();
        }
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "券列表_" + System.currentTimeMillis() + ".xls");
        Boolean right = ContextedPermission.hasPermission("SEE_ALL_SUPPLIER");
        Long id = OperateRbac.currentUser().id;
        JPAExtPaginator<ECoupon> couponsList;
        if (!Play.runingInTestMode()) {
            couponsList = ECoupon.query(condition, 1, PAGE_SIZE, id, right);
        } else {
            couponsList = ECoupon.query(condition, 1, PAGE_SIZE, null, true);
        }
        for (ECoupon coupon : couponsList) {
            coupon.shopName = coupon.getConsumedShop();

            String clerkInfo = "";
            if (coupon.supplierUser != null) {
                if (coupon.supplierUser.userName != null) {
                    clerkInfo += coupon.supplierUser.userName;
                }
                if (coupon.supplierUser.jobNumber != null) {
                    clerkInfo += "(工号：" + coupon.supplierUser.jobNumber + ")";
                }
            }
            coupon.clerkInfo = clerkInfo;

            if (coupon.verifyType == VerifyCouponType.SHOP) {
                coupon.verifyName = "门店验证";
            } else if (coupon.verifyType == VerifyCouponType.CLERK_MESSAGE) {
                coupon.verifyName = "店员短信验证";
            } else if (coupon.verifyType == VerifyCouponType.CONSUMER_MESSAGE) {
                coupon.verifyName = "消费者短信验证";
            } else if (coupon.verifyType == VerifyCouponType.TELEPHONE) {
                coupon.verifyName = "电话验证";
            } else if (coupon.verifyType == VerifyCouponType.OP_VERIFY) {
                coupon.verifyName = "运营代理验证";
            }
            if (coupon.status == ECouponStatus.UNCONSUMED) {
                coupon.statusInfo = "未消费";
            } else if (coupon.status == ECouponStatus.CONSUMED) {
                coupon.statusInfo = "已消费";
            } else if (coupon.status == ECouponStatus.REFUND) {
                coupon.statusInfo = "已退款";
            }
            coupon.eCouponSn = coupon.getMaskedEcouponSn();
        }

        render(couponsList);

    }


}
