package controllers;

import com.uhuila.common.util.DateUtil;
import controllers.supplier.SupplierInjector;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.sales.Shop;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * User: yan
 * Date: 13-6-6
 * Time: 下午5:35
 */
@With({SupplierRbac.class, SupplierInjector.class})
public class SupplierAppointments extends Controller {
    @Before(priority = 1000)
    public static void storeShopIp() {
        SupplierUser supplierUser = SupplierRbac.currentUser();
        String strShopId = request.params.get("shopId");
        if (StringUtils.isNotBlank(strShopId)) {
            try {
                Long shopId = Long.parseLong(strShopId);
                if (supplierUser.lastShopId == null || !supplierUser.lastShopId.equals(shopId)) {
                    supplierUser.lastShopId = shopId;
                    supplierUser.save();
                }
            } catch (Exception e) {
                //ignore
            }
        }
        if (supplierUser.lastShopId != null) {
            renderArgs.put("shopId", supplierUser.lastShopId);
        }
    }

    /**
     * 预约页面
     */
    public static void index() {
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        Long supplierUserId = SupplierRbac.currentUser().id;
        SupplierUser supplierUser = SupplierUser.findById(supplierUserId);
        List<Shop> shopList = Shop.findShopBySupplier(supplierId);

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
     * @param couponSn 券号
     */
    public static void showAppointmentCoupon(Long shopId, String couponSn) {
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        List<Shop> shopList = Shop.findShopBySupplier(supplierId);

        couponSn = StringUtils.trim(couponSn);

        if (StringUtils.isBlank(couponSn)) {
            Validation.addError("error-info", "券号不能为空");
        }
        renderError(shopId, couponSn, shopList);

        //根据页面录入券号查询对应信息
        ECoupon ecoupon = ECoupon.query(couponSn, supplierId);
        if (ecoupon == null) {
            Validation.addError("error-info", "没有找到符合的券信息");
        }
        renderError(shopId, couponSn, shopList);

//        ecoupon = ECoupon.findById(63969L);
        //check券和门店
        String errorInfo = ECoupon.getECouponStatusDescription(ecoupon, shopId);
        if (StringUtils.isNotEmpty(errorInfo)) {
            Validation.addError("error-info", errorInfo);
        }

        renderError(shopId, couponSn, shopList);

        render("SupplierAppointments/index.html", shopId, couponSn, ecoupon, shopList);
    }

    private static void renderError(Long shopId, String couponSn, List<Shop> shopList) {
        if (Validation.hasErrors()) {
            render("SupplierAppointments/index.html", shopId, couponSn, shopList);
        }
    }

}
