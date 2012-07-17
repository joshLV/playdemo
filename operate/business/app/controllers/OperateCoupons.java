package controllers;

import models.order.CouponsCondition;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.VerifyCouponType;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

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

        JPAExtPaginator<ECoupon> couponPage = ECoupon.query(condition, pageNumber, PAGE_SIZE);
        render(couponPage, condition);
    }

    /**
     * 冻结此券
     *
     * @param id
     */
    public static void freeze(long id) {
        ECoupon.freeze(id);
        index(null);
    }

    /**
     * 解冻此券
     *
     * @param id
     */
    public static void unfreeze(long id) {
        ECoupon.unfreeze(id);
        index(null);
    }


    /**
     * 重发短信
     *
     * @param id
     */
    public static void sendMessage(long id) {
        boolean sendFalg = ECoupon.sendMessage(id);
        renderJSON(sendFalg ? "0" : "1");
    }

    public static void couponExcelOut(CouponsCondition condition) {

        if (condition == null) {
            condition = new CouponsCondition();
        }
        String page = request.params.get("page");
        request.format = "xls";
        String __EXCEL_FILE_NAME__ = "券列表_" + System.currentTimeMillis() + "xls";
        renderArgs.put("__EXCEL_FILE_NAME__", __EXCEL_FILE_NAME__);
        JPAExtPaginator<ECoupon> couponsList = ECoupon.query(condition, 1, PAGE_SIZE);
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
            }
            if (coupon.status == ECouponStatus.UNCONSUMED) {
                coupon.statusInfo = "未消费";
            } else if (coupon.status == ECouponStatus.CONSUMED) {
                coupon.statusInfo = "已消费";
            } else if (coupon.status == ECouponStatus.REFUND) {
                coupon.statusInfo = "已退款";
            }
            coupon.save();
        }

        render(__EXCEL_FILE_NAME__, couponsList);

    }
}
