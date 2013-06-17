package controllers;

import com.uhuila.common.util.DateUtil;
import controllers.supplier.SupplierInjector;
import models.admin.SupplierUser;
import models.order.*;
import models.sales.Shop;
import models.sms.SMSMessage;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import play.data.validation.Validation;
import play.db.jpa.JPA;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.*;

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
    public static void index(String phone, Date appointmentDate) {
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        Long supplierUserId = SupplierRbac.currentUser().id;
        SupplierUser supplierUser = SupplierUser.findById(supplierUserId);
        List<Shop> shopList = Shop.findShopBySupplier(supplierId);

        if (shopList.size() == 0) {
            error("该商户没有添加门店信息！");
        }
        StringBuilder sql = new StringBuilder("select e from ECoupon e where e.status=:status and e.goods.supplierId=:supplierId ");
        Map<String, Object> params = new HashMap<>();
        params.put("status", ECouponStatus.UNCONSUMED);
        params.put("supplierId", supplierId);

        if (StringUtils.isNotBlank(phone)) {
            sql.append(" and e.orderItems.phone =:phone");
            params.put("phone", phone);
        }
        if (appointmentDate == null) {
            sql.append(" and e.appointmentDate >=:appointmentDate");
            params.put("appointmentDate", DateUtils.truncate(new Date(), Calendar.DATE));
        } else {
            sql.append(" and e.appointmentDate >=:appointmentDate");
            params.put("appointmentDate", appointmentDate);
        }
        sql.append(" order by e.appointmentDate desc ,e.id desc");
        Query query = JPA.em().createQuery(sql.toString());

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        List<ECoupon> couponList = query.getResultList();

        render(shopList, supplierUser, couponList, appointmentDate, phone);
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
        checkInputItems(shopId, couponSn, appointmentDate);

        //根据页面录入券号查询对应信息
        ECoupon ecoupon = ECoupon.query(couponSn, supplierId);
        //事先验证券的信息
        checkCouponInfos(ecoupon, shopId);

        //预约处理
        if (!ecoupon.appointment(appointmentDate, appointmentRemark, shopId, supplierUser)) {
            Validation.addError("error-info", "预约失败，请联系技术人员查看！");
        }
        renderError(shopId);

        index(null, null);
    }

    public static void showEdit(Long id) {
        ECoupon coupon = ECoupon.findById(id);
        render(coupon);
    }

    public static void update(Long couponId, Date appointmentDate, String appointmentRemark) {
        ECoupon coupon = ECoupon.findById(couponId);
        if (coupon == null) {
            Validation.addError("error-info", "没有找到符合的券信息");
        }
        if (Validation.hasErrors()) {
            render("SupplierAppointments/showEdit.html");
        }
        if (appointmentDate !=null && appointmentDate.compareTo(coupon.appointmentDate) != 0) {
            coupon.appointmentDate = appointmentDate;
            coupon.appointmentRemark = appointmentRemark;
            coupon.save();
            OrderECouponMessage.with(coupon).operator(SupplierRbac.currentUser().userName).remark("重新预约日期信息").sendToMQ();
        }
        index(null, null);
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
        String errorInfo = ECoupon.getECouponStatusDescription(ecoupon, shopId,"supplierVerify");
        if (StringUtils.isNotEmpty(errorInfo)) {
            renderJSON("{\"errorInfo\":\"" + errorInfo + "\"}");
        }
        renderJSON("{\"isOk\":\"true\"}");
    }

    private static void checkInputItems(Long shopId, String couponSn, Date appointmentDate) {
        couponSn = StringUtils.trim(couponSn);

        if (StringUtils.isBlank(couponSn)) {
            Validation.addError("error-info", "券号不能为空");
        }
        if (appointmentDate == null) {
            Validation.addError("error-info", "请选择预约日期");
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
        String errorInfo = ECoupon.getECouponStatusDescription(ecoupon, shopId,"supplierVerify");
        if (StringUtils.isNotEmpty(errorInfo)) {
            Validation.addError("error-info", errorInfo);
        }

        renderError(shopId);

        //没有错误信息
        renderArgs.put("ecoupon", ecoupon);
    }

    private static void renderError(Long shopId) {
        if (Validation.hasErrors()) {
            render("SupplierAppointments/showAdd.html", shopId);
        }
    }

}
