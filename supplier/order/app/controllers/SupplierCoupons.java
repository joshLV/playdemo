package controllers;

import com.uhuila.common.util.DateUtil;
import models.admin.SupplierUser;
import models.order.CouponsCondition;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.VerifyCouponType;
import models.sales.ConsultRecord;
import models.sales.ConsultResultCondition;
import models.sales.Shop;
import models.sms.SMSUtil;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@With(SupplierRbac.class)
public class SupplierCoupons extends Controller {

    public static int PAGE_SIZE = 15;

    /**
     * 验证页面
     */
    @ActiveNavigation("coupons_verify")
    public static void verify() {
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        Long supplierUserId = SupplierRbac.currentUser().id;
        SupplierUser supplierUser = SupplierUser.findById(supplierUserId);
        List shopList = Shop.findShopBySupplier(supplierId);
        if (shopList.size() == 0) {
            error("该商户没有添加门店信息！");
        }
        if (supplierUser.shop == null) {
            render(shopList, supplierUser);
        } else {
            Shop shop = supplierUser.shop;

            //根据页面录入券号查询对应信息
            render(shop, supplierUser);
        }
    }


    /**
     * 查询
     *
     * @param eCouponSn 券号
     */
    @ActiveNavigation("coupons_verify")
    public static void query(Long shopId, String eCouponSn) {
        if (Validation.hasErrors()) {
            render("../views/SupplierCoupons/index.html", eCouponSn);
        }

        Long supplierId = SupplierRbac.currentUser().supplier.id;

        //根据页面录入券号查询对应信息
        ECoupon ecoupon = ECoupon.query(eCouponSn, supplierId);

        render("/SupplierCoupons/consume.html", shopId, ecoupon);
    }

    /**
     * 修改券状态,并产生消费交易记录
     *
     * @param eCouponSn 券号
     */
    @ActiveNavigation("coupons_verify")
    public static void update(Long shopId, String eCouponSn, String shopName) {
        if (Validation.hasErrors()) {
            render("../views/SupplierCoupons/index.html", eCouponSn);
        }

        Long supplierId = SupplierRbac.currentUser().supplier.id;
        ECoupon eCoupon = ECoupon.query(eCouponSn, supplierId);
        //根据页面录入券号查询对应信息,并产生消费交易记录
        if (eCoupon == null) {
            renderJSON("err");
        }
        if (eCoupon.status == ECouponStatus.UNCONSUMED) {
            //冻结的券
            if (eCoupon.isFreeze == 1) {
                renderJSON("3");
            }
            if (!eCoupon.isBelongShop(shopId)) {
                renderJSON("1");
            }
            //不再验证时间范围内
            if (!eCoupon.checkVerifyTimeRegion(new Date())) {
                String info = eCoupon.getCheckInfo();
                renderJSON("{\"error\":\"2\",\"info\":\"" + info + "\"}");
            }
            if (!eCoupon.consumeAndPayCommission(shopId, null, SupplierRbac.currentUser(), VerifyCouponType.SHOP)) {
                renderJSON("4");
            }
            String dateTime = DateUtil.getNowTime();
            String coupon = eCoupon.getLastCode(4);

            // 发给消费者
            SMSUtil.send("【一百券】您尾号" + coupon + "的券号于" + dateTime
                    + "已成功消费，使用门店：" + shopName + "。如有疑问请致电：400-6262-166", eCoupon.orderItems.phone, eCoupon.replyCode);
        } else {
            renderJSON(eCoupon.status);
        }

        renderJSON("0");
    }

    /**
     * 券号列表
     */
    @ActiveNavigation("coupons_index")
    public static void index(CouponsCondition condition) {
        if (condition == null) {
            condition = new CouponsCondition();
        }

        condition.supplier = SupplierRbac.currentUser().supplier;

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        JPAExtPaginator<ECoupon> couponPage = ECoupon.query(condition, pageNumber, PAGE_SIZE);
        render(couponPage, condition);
    }

    public static void couponExcelOut(CouponsCondition condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if (condition == null) {
            condition = new CouponsCondition();
        }
        condition.supplier = SupplierRbac.currentUser().supplier;
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "券内容列表_" + System.currentTimeMillis() + ".xls");
        JPAExtPaginator<ECoupon> couponPage = ECoupon.query(condition, pageNumber, PAGE_SIZE);
        for (ECoupon coupon : couponPage) {
            String staff = "";
            coupon.verifyTypeInfo = Messages.get("coupon." + coupon.verifyType);
            coupon.statusInfo = Messages.get("coupon." + coupon.status);
            if (coupon.supplierUser != null) {
                if (StringUtils.isNotBlank(coupon.supplierUser.userName))
                    staff = coupon.supplierUser.userName;
                if (StringUtils.isNotBlank(coupon.supplierUser.jobNumber))
                    staff += "(工号:" + coupon.supplierUser.jobNumber + ")";
            }
            coupon.staff = staff;
        }
        render(couponPage, condition);
    }
}
