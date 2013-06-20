package controllers;

import controllers.supplier.SupplierInjector;
import models.admin.SupplierUser;
import models.order.CouponsCondition;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.sales.Shop;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.i18n.Messages;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@With({SupplierRbac.class, SupplierInjector.class})
public class SupplierECoupons extends Controller {

    public static int PAGE_SIZE = 10;

    /**
     * 券号列表
     */
    @ActiveNavigation("coupons_index")
    public static void index(CouponsCondition condition) {
        if (condition == null) {
            condition = new CouponsCondition();
        }

        SupplierUser supplierUser = SupplierRbac.currentUser();
        Long supplierId = supplierUser.supplier.id;
        List<Shop> shopList;
        if (supplierUser.shop != null) {
            condition.shopId = supplierUser.shop.id;
            shopList = new ArrayList<>();
            shopList.add(supplierUser.shop);
        } else {
            shopList = Shop.findShopBySupplier(supplierId);
        }
        condition.supplier = supplierUser.supplier;

        condition.status = ECouponStatus.CONSUMED;

        int pageNumber = getPageNumber();

        JPAExtPaginator<ECoupon> couponPage = ECoupon.findByCondition(condition, pageNumber, PAGE_SIZE);
        render(couponPage, condition, shopList);
    }

    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }

    public static void couponExcelOut(CouponsCondition condition) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = new CouponsCondition();
        }
        condition.supplier = SupplierRbac.currentUser().supplier;
        condition.status = ECouponStatus.CONSUMED;
        SimpleDateFormat dateFormat = new SimpleDateFormat("M月d日");
        SimpleDateFormat dateFormat0 = new SimpleDateFormat(Order.COUPON_EXPIRE_FORMAT);
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "券内容列表_" + System.currentTimeMillis() + ".xls");
        JPAExtPaginator<ECoupon> couponPage = ECoupon.query(condition, pageNumber, PAGE_SIZE);
        for (ECoupon coupon : couponPage) {
            String staff = "";
            coupon.verifyTypeInfo = Messages.get("coupon." + coupon.verifyType);
            coupon.statusInfo = Messages.get("coupon." + coupon.status);
            if (coupon.supplierUser != null) {
                if (StringUtils.isNotBlank(coupon.supplierUser.userName)) {
                    staff = coupon.supplierUser.userName;
                } else {
                    staff = coupon.supplierUser.loginName;
                }
//                if (StringUtils.isNotBlank(coupon.supplierUser.jobNumber))
//                    staff += "(工号:" + coupon.supplierUser.jobNumber + ")";
            }
            coupon.staff = staff;
            if (coupon.appointmentDate != null) {
                if (coupon.goods.isKtvProduct()) {
                    coupon.excelRemarks = "预订：" + dateFormat.format(coupon.appointmentDate) + "\r" + coupon.appointmentRemark;
                } else {
                    coupon.excelRemarks = "预约日期：" + dateFormat0.format(coupon.appointmentDate) + "\r" + coupon.appointmentRemark;
                }
            }
            try {
                coupon.expireAt = dateFormat.parse(dateFormat0.format(coupon.expireAt));
                coupon.consumedAt = dateFormat.parse(dateFormat0.format(coupon.consumedAt));
            } catch (ParseException e) {
            }
        }
        render(couponPage, condition);
    }
}
