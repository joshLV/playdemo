package controllers;

import com.uhuila.common.util.DateUtil;
import controllers.supplier.SupplierInjector;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponHistoryMessage;
import models.order.ECouponStatus;
import models.order.OrderECouponMessage;
import models.sales.Shop;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Calendar;
import java.util.Date;
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
     * 已预约列表
     */
    public static void index() {
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        Long supplierUserId = SupplierRbac.currentUser().id;
        SupplierUser supplierUser = SupplierUser.findById(supplierUserId);
        List<Shop> shopList = Shop.findShopBySupplier(supplierId);

        if (shopList.size() == 0) {
            error("该商户没有添加门店信息！");
        }

        List<ECoupon> couponList = ECoupon.find("appointmentDate >=? and status=? and goods.supplierId=?",
                DateUtils.truncate(new Date(), Calendar.DATE), ECouponStatus.UNCONSUMED, supplierId).fetch();
        render(shopList, supplierUser, couponList);
    }

    public static void showAdd() {
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        List<Shop> shopList = Shop.findShopBySupplier(supplierId);

        if (shopList.size() == 0) {
            error("该商户没有添加门店信息！");
        }
        render(shopList);
    }

    public static void create(Long shopId, String couponSn, Date appointmentDate, String appointmentRemark) {
        SupplierUser supplierUser = SupplierRbac.currentUser();
        Long supplierId = supplierUser.supplier.id;

        List<Shop> shopList = Shop.findShopBySupplier(supplierId);
        renderArgs.put("shopList", shopList);
        renderArgs.put("couponSn", couponSn);

        //检查是否输入预约券号
        isInputCouponSn(shopId, couponSn);

        //根据页面录入券号查询对应信息
        ECoupon ecoupon = ECoupon.query(couponSn, supplierId);
        //事先验证券的信息
        checkCouponInfos(ecoupon, shopId);

        //预约处理
        ecoupon.appointment(appointmentDate, appointmentRemark, shopId,supplierUser);


        render("SupplierAppointments/index.html", shopId, couponSn);

    }


    /**
     * 验证券信息
     */
    public static void verifyCoupon(Long shopId, String couponSn) {
        Long supplierId = SupplierRbac.currentUser().supplier.id;

        couponSn = StringUtils.trim(couponSn);

        if (StringUtils.isBlank(couponSn)) {
            renderJSON("{\"errorInfo\":\"券号不能为空！\"}");
        }
        //根据页面录入券号查询对应信息
        ECoupon ecoupon = ECoupon.query(couponSn, supplierId);

        //check券和门店
        String errorInfo = ECoupon.getECouponStatusDescription(ecoupon, shopId);
        if (StringUtils.isNotEmpty(errorInfo)) {
            renderJSON("{\"errorInfo\":\"" + errorInfo + "\"}");
        }
        renderJSON("{\"isOk\":\"true\"}");
    }

    private static void isInputCouponSn(Long shopId, String couponSn) {
        couponSn = StringUtils.trim(couponSn);

        if (StringUtils.isBlank(couponSn)) {
            Validation.addError("error-info", "券号不能为空");
        }
        renderError(shopId);
    }

    /**
     * 验证券的信息
     */
    private static void checkCouponInfos(ECoupon ecoupon, Long shopId) {
        if (ecoupon == null) {
            Validation.addError("error-info", "没有找到符合的券信息");
        }
        renderError(shopId);

        //check券和门店
        String errorInfo = ECoupon.getECouponStatusDescription(ecoupon, shopId);
        if (StringUtils.isNotEmpty(errorInfo)) {
            Validation.addError("error-info", errorInfo);
        }

        renderError(shopId);

        //没有错误信息
        renderArgs.put("ecoupon", ecoupon);
    }

    private static void renderError(Long shopId) {
        if (Validation.hasErrors()) {
            render("SupplierAppointments/index.html", shopId);
        }
    }

}
