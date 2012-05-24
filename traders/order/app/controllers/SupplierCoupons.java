package controllers;

import models.admin.SupplierSetting;
import models.admin.SupplierUser;
import models.order.CouponsCondition;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.sales.Shop;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;
import java.util.Map;

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
        SupplierSetting supplierSetting = SupplierSetting.getSetting(supplierUserId);

        render(shopList, supplierUser, supplierSetting);
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
        Map<String, Object> queryMap = ECoupon.queryInfo(eCouponSn, supplierId, shopId);
        renderJSON(queryMap);
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
            eCoupon.consumed(shopId, SupplierRbac.currentUser());
        } else {
            renderJSON(eCoupon.status);
        }

        SupplierSetting supplierSetting = new SupplierSetting();
        supplierSetting.save(SupplierRbac.currentUser().id, shopId, shopName);
        renderJSON("0");
    }

    /**
     * 券号列表
     */
    @ActiveNavigation("coupons_index")
    public static void index(CouponsCondition condition) {
        if (condition == null){
            condition = new CouponsCondition();
        }
        condition.supplier = SupplierRbac.currentUser().supplier;

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        JPAExtPaginator<ECoupon> couponPage = ECoupon.query(condition, pageNumber, PAGE_SIZE);
        render(couponPage, condition);
    }
}
