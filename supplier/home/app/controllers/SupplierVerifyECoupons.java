package controllers;

import com.uhuila.common.util.DateUtil;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.VerifyCouponType;
import models.sales.Shop;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 电子券验证.
 * <p/>
 * <p/>
 * User: sujie
 * Date: 12/19/12
 * Time: 9:59 AM
 */
@With(SupplierRbac.class)
@ActiveNavigation("coupons_single_index")
public class SupplierVerifyECoupons extends Controller {


    @Before(priority = 1000)
    public static void storeShopIp() {
        SupplierUser supplierUser = SupplierRbac.currentUser();
        String strShopId = request.params.get("shopId");
        if (StringUtils.isNotBlank(strShopId)) {
            try {
                Long shopId = Long.parseLong(strShopId);
                if (supplierUser.lastShopId == null || supplierUser.lastShopId != shopId) {
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
     * 券验证页面
     */
    public static void index() {
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
    public static void singleQuery(Long shopId, String eCouponSn) {
        Long supplierId = SupplierRbac.currentUser().supplier.id;

        eCouponSn = StringUtils.trim(eCouponSn);
        //根据页面录入券号查询对应信息
        ECoupon ecoupon = ECoupon.query(eCouponSn, supplierId);

        //check券和门店
        String errorInfo = checkCoupon(ecoupon, shopId);
        System.out.println(errorInfo);
        if (StringUtils.isNotEmpty(errorInfo)) {
            renderJSON("{\"errorInfo\":\"" + errorInfo + "\"}");
        } else {
            Supplier supplier = Supplier.findById(supplierId);
            renderJSON("{\"supplierName\":\"" + supplier.getName() + "\",\"faceValue\":" + ecoupon.faceValue
                    + ",\"expireAt\":\"" + DateUtil.dateToString(ecoupon.expireAt, 0) + "\"}");
        }
    }

    /**
     * 验证券
     */
    public static void singleVerify(Long shopId, String eCouponSn) {
        Long supplierUserId = SupplierRbac.currentUser().id;
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        SupplierUser supplierUser = SupplierUser.findById(supplierUserId);
        List<Shop> shopList = Shop.findShopBySupplier(supplierId);
        ECoupon ecoupon = ECoupon.query(eCouponSn, supplierId);
        Shop shop = Shop.findById(shopId);

        //check券和门店
        String errorInfo = ECoupon.getECouponStatusDescription(ecoupon, shopId);
        if (StringUtils.isNotEmpty(errorInfo)) {
            render("SupplierVerifyECoupons/index.html", shop, ecoupon, supplierUser, shopList, errorInfo);
        }

        if (ecoupon.status == ECouponStatus.UNCONSUMED) {
            if (!ecoupon.consumeAndPayCommission(shopId, null, SupplierRbac.currentUser(), VerifyCouponType.SHOP)) {
                errorInfo = "第三方" + ecoupon.partner + "券验证失败！请确认券状态(是否过期或退款等)！";
            }
            if (StringUtils.isNotEmpty(errorInfo)) {
                render("SupplierVerifyECoupons/index.html", shop, ecoupon, supplierUser, shopList, errorInfo);
            }
            String dateTime = DateUtil.getNowTime();
            String ecouponSNLast4Code = ecoupon.getLastCode(4);
            // 发给消费者
            SMSUtil.send2("【一百券】您尾号" + ecouponSNLast4Code + "的券号于" + dateTime
                    + "已成功消费，使用门店：" + shop.name + "。如有疑问请致电：400-6262-166", ecoupon.orderItems.phone, ecoupon.replyCode);
        }

        String successInfo = "券号" + eCouponSn + "，已成功验证！查看<a href=\"/coupons\">已验证券号</a>";
        render("SupplierVerifyECoupons/index.html", shop, ecoupon, supplierUser, shopList, successInfo);
    }

    private static String checkCoupon(ECoupon ecoupon, Long shopId) {
        String errorInfo = null;
        if (ecoupon == null) {
            errorInfo = "对不起，没有该券的信息！";
        } else if (shopId == null) {
            errorInfo = "对不起，该券不能在此门店使用!";
        }
        return errorInfo;
    }
}