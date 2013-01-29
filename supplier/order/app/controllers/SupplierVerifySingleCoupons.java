package controllers;

import com.uhuila.common.util.DateUtil;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.VerifyCouponType;
import models.sales.Shop;
import models.sms.SMSUtil;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.db.jpa.JPA;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-11-29
 * Time: 上午10:43
 */
@With(SupplierRbac.class)
@ActiveNavigation("coupons_single_index")
public class SupplierVerifySingleCoupons extends Controller {

    @Before(priority=1000)
    public static void storeShopIp() {
        SupplierUser supplierUser = SupplierRbac.currentUser();
        String strShopId = request.params.get("shopId");
        System.out.println("hello storeshopid=" + strShopId + ", lastShopId=" + supplierUser.lastShopId);
        if (StringUtils.isNotBlank(strShopId)) {
            Long shopId = Long.parseLong(strShopId);
            if (supplierUser.lastShopId == null || supplierUser.lastShopId != shopId) {
                supplierUser.lastShopId = shopId;
                supplierUser.save();
            }
        }
        if (supplierUser.lastShopId != null) {
            renderArgs.put("shopId", supplierUser.lastShopId);
        }
    }

    /**
     * 券验证页面
     */
    public static void index() {
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        SupplierUser supplierUser = SupplierRbac.currentUser();

        // 如果跳转过新验证界面，使用之
        if ("v2".equals(supplierUser.defaultUiVersion)) {
            redirect("/ui-version/to/v2");
        }

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
    public static void singleQuery(Long shopId, String eCouponSn) {
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        Long supplierUserId = SupplierRbac.currentUser().id;
        SupplierUser supplierUser = SupplierUser.findById(supplierUserId);
        List<Shop> shopList = Shop.findShopBySupplier(supplierId);

        eCouponSn = StringUtils.trim(eCouponSn);
        //根据页面录入券号查询对应信息
        ECoupon ecoupon = ECoupon.query(eCouponSn, supplierId);
        Shop shop = Shop.findById(shopId);
        //check券和门店
        checkCoupon(ecoupon, shopId, shopList, supplierUser);

        String ecouponStatusDescription = ECoupon.getECouponStatusDescription(ecoupon, shopId);

        render("SupplierVerifySingleCoupons/index.html", ecoupon, shop, supplierUser, shopList, ecouponStatusDescription);
    }

    /**
     * 验证券
     */
    public static void singleVerify() {
        Long supplierUserId = SupplierRbac.currentUser().id;
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        Long shopId = Long.valueOf(request.params.get("shopId"));
        String eCouponSn = request.params.get("eCouponSn");
        JPA.em().flush();

        SupplierUser supplierUser = SupplierUser.findById(supplierUserId);
        List<Shop> shopList = Shop.findShopBySupplier(supplierId);
        ECoupon ecoupon = ECoupon.query(eCouponSn, supplierId);
        //check券和门店
        checkCoupon(ecoupon, shopId, shopList, supplierUser);

        String ecouponStatusDescription = ECoupon.getECouponStatusDescription(ecoupon, shopId);
        if (ecouponStatusDescription != null) {
            Validation.addError("error-info", ecouponStatusDescription);
        }

        Shop shop = Shop.findById(shopId);
        if (Validation.hasErrors()) {
            render("SupplierVerifySingleCoupons/index.html", shop, ecoupon, supplierUser, shopList);
        }

        if (ecoupon.status == ECouponStatus.UNCONSUMED) {
            if (!ecoupon.consumeAndPayCommission(shopId, null, SupplierRbac.currentUser(), VerifyCouponType.SHOP)) {
                Validation.addError("error-info", "第三方" + ecoupon.partner + "券验证失败！请确认券状态(是否过期或退款等)！");
            }
            if (Validation.hasErrors()) {
                render("SupplierVerifySingleCoupons/index.html", shop, ecoupon, supplierUser, shopList);
            }
            String dateTime = DateUtil.getNowTime();
            String ecouponSNLast4Code = ecoupon.getLastCode(4);
            // 发给消费者
            SMSUtil.send2("【一百券】您尾号" + ecouponSNLast4Code + "的券号于" + dateTime
                    + "已成功消费，使用门店：" + shop.name + "。如有疑问请致电：400-6262-166", ecoupon.orderItems.phone, ecoupon.replyCode);
        }

        renderArgs.put("success_info", "true");
        render("SupplierVerifySingleCoupons/index.html", shop, ecoupon, supplierUser, shopList);

    }

    private static void checkCoupon(ECoupon ecoupon, Long shopId, List<Shop> shopList, SupplierUser supplierUser) {
        if (ecoupon == null) {
            Validation.addError("error-info", "对不起，没有该券的信息！");
        }
        if (shopId == null) {
            Validation.addError("error-info", "对不起，该券不能在此门店使用!");
        }

        Shop shop = Shop.findById(shopId);
        if (Validation.hasErrors()) {
            render("SupplierVerifySingleCoupons/index.html", shop, ecoupon, supplierUser, shopList);
        }
    }
}
