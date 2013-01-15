package controllers;

import models.accounts.AccountType;
import models.admin.OperateUser;
import models.order.CouponHistory;
import models.order.CouponsCondition;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.VerifyCouponType;
import models.sales.Brand;
import operate.rbac.ContextedPermission;
import operate.rbac.annotations.ActiveNavigation;
import operate.rbac.annotations.Right;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;
import util.DateHelper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@With(OperateRbac.class)
public class OperateCoupons extends Controller {

    public static int PAGE_SIZE = 15;

    /**
     * 券号列表
     */
    @ActiveNavigation("coupons_index")
    public static void index(CouponsCondition condition) {
        Boolean hasEcouponRefundPermission = ContextedPermission.hasPermission("ECOUPON_REFUND");

        if (condition == null) {
            condition = new CouponsCondition();
            condition.hidPaidAtBegin = DateHelper.beforeDays(1);
            condition.hidPaidAtEnd = new Date();
        }
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        condition.hasSeeAllSupplierPermission = ContextedPermission.hasPermission("SEE_ALL_SUPPLIER");
        condition.operatorId = OperateRbac.currentUser().id;
        Boolean hasSeeAllSupplierPermission = ContextedPermission.hasPermission("SEE_ALL_SUPPLIER");
        Long operatorId = OperateRbac.currentUser().id;
        JPAExtPaginator<ECoupon> couponPage;

        couponPage = ECoupon.query(condition, pageNumber, PAGE_SIZE);
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
        render(couponPage, condition, amountSummary, hasRight, hasEcouponRefundPermission);
    }

    /**
     * 冻结此券
     *
     * @param id
     */
    @ActiveNavigation("coupons_index")
    public static void freeze(long id) {
        ECoupon.freeze(id, OperateRbac.currentUser().userName);
        index(null);
    }

    /**
     * 解冻此券
     *
     * @param id
     */
    @Right("UNFREEZE_COUPON")
    public static void unfreeze(long id) {
        ECoupon.unfreeze(id, OperateRbac.currentUser().userName);
        index(null);
    }

    /**
     * 券号列表
     */
    @ActiveNavigation("coupons_index")
    public static void couponHistory(Long couponId) {
        ECoupon coupon = ECoupon.findById(couponId);
        if (coupon == null) {
            return;
        }

        List<CouponHistory> couponList = CouponHistory.find("coupon=?", coupon).fetch();
        String couponSn = coupon.getMaskedEcouponSn();
        render("OperateCoupons/history.html", couponSn, couponList);
    }

    @Right("ECOUPON_REFUND")
    public static void refund(Long couponId) {
        ECoupon coupon = ECoupon.findById(couponId);
        Boolean couponNoRefund = false;
        if (coupon.goods.noRefund != null && coupon.goods.noRefund == true) {
            couponNoRefund = true;
        }
        render("OperateCoupons/refund.html", couponNoRefund, coupon, couponId);
    }

    @Right("ECOUPON_REFUND")
    public static void handleRefund(Long couponId, String refundComment) {
        if (StringUtils.isBlank(refundComment)) {
            Validation.addError("refundComment", "备注不能为空");
        }
        ECoupon ecoupon = ECoupon.findById(couponId);
        if (Validation.hasErrors()) {
            ECoupon coupon = ECoupon.findById(couponId);
            Boolean couponNoRefund = false;
            if (coupon.goods.noRefund != null && coupon.goods.noRefund == true) {
                couponNoRefund = true;
            }
            render("OperateCoupons/refund.html", couponNoRefund, coupon, couponId, refundComment);
        }
        String returnFlg = "";
        if (ecoupon.status == ECouponStatus.UNCONSUMED && ecoupon.order.userType == AccountType.CONSUMER) {
            returnFlg = ECoupon.applyRefund(ecoupon, ecoupon.order.userId, AccountType.CONSUMER, OperateRbac.currentUser().userName, refundComment);
        }
        if (ecoupon.status == ECouponStatus.UNCONSUMED && ecoupon.order.userType == AccountType.RESALER) {
            returnFlg = ECoupon.applyRefund(ecoupon, ecoupon.order.userId, AccountType.RESALER, OperateRbac.currentUser().userName, refundComment);
        }
        String message = "";
        if (returnFlg == "{\"error\":\"ok\"}") {
            message = "电子券退款成功";
            render(message);
        } else {
            message = "电子券退款失败，请重新操作";
            render(message);
        }
    }


    /**
     * 重发短信
     *
     * @param id
     */
    @ActiveNavigation("coupons_index")
    public static void sendMessage(long id) {
        boolean sendFalg = ECoupon.sendMessage(id);
        ECoupon eCoupon = ECoupon.findById(id);
        new CouponHistory(eCoupon, OperateRbac.currentUser().userName, "重发短信", eCoupon.status, eCoupon.status, null).save();
        renderJSON(sendFalg ? "0" : "1");
    }

    @ActiveNavigation("coupons_index")
    public static void couponExcelOut(CouponsCondition condition) {

        if (condition == null) {
            condition = new CouponsCondition();
        }
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "券列表_" + System.currentTimeMillis() + ".xls");
        condition.hasSeeAllSupplierPermission = ContextedPermission.hasPermission("SEE_ALL_SUPPLIER");
        condition.operatorId = OperateRbac.currentUser().id;
        JPAExtPaginator<ECoupon> couponsList;


        couponsList = ECoupon.query(condition, 1, PAGE_SIZE);

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

    @ActiveNavigation("coupons_index")
    public static void showAppointment(Long couponId) {
        ECoupon coupon = ECoupon.findById(couponId);
        String err = null;
        if (coupon == null){
            err = "找不着这个券啊";
        }else if( coupon.status != ECouponStatus.UNCONSUMED){
            err = "只有未消费的券才能预约啊亲";
        } else if(!coupon.goods.isOrder) {
            err = "这个商品不需要预约的拉";
        }
        render(coupon, err);
    }
    @ActiveNavigation("coupons_index")
    public static void appointment(Long couponId, Date date, String remark) {
        ECoupon coupon = ECoupon.findById(couponId);
        if (coupon == null || coupon.status != ECouponStatus.UNCONSUMED || !coupon.goods.isOrder) {
            notFound();return;
        }
        coupon.appointmentDate = date;
        coupon.appointmentRemark = remark;
        coupon.save();
        boolean success = true;
        render("OperateCoupons/showAppointment.html", coupon, success);
    }
}
