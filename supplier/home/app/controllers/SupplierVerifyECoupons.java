package controllers;

import com.uhuila.common.util.DateUtil;
import controllers.supplier.SupplierInjector;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.VerifyCouponType;
import models.sales.Shop;
import models.sms.SMSUtil;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;

/**
 * 电子券验证.
 * <p/>
 * <p/>
 * User: sujie
 * Date: 12/19/12
 * Time: 9:59 AM
 */
@With({SupplierRbac.class, SupplierInjector.class})
@ActiveNavigation("coupons_multi_index")
public class SupplierVerifyECoupons extends Controller {
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
     * 券验证页面
     */
    public static void index() {
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        Long supplierUserId = SupplierRbac.currentUser().id;
        SupplierUser supplierUser = SupplierUser.findById(supplierUserId);
        List<Shop> shopList = Shop.findShopBySupplier(supplierId);
        List<String> verifiedCoupons = ECoupon.getRecentVerified(supplierUser, 5);
        renderArgs.put("verifiedCoupons", verifiedCoupons);

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
    public static void singleQuery(Long shopId, String eCouponSn) {
        Long supplierId = SupplierRbac.currentUser().supplier.id;

        eCouponSn = StringUtils.trim(eCouponSn);
        //根据页面录入券号查询对应信息
        ECoupon ecoupon = ECoupon.query(eCouponSn, supplierId);

        //check券和门店
        String errorInfo = ECoupon.getECouponStatusDescription(ecoupon, shopId);
        if (StringUtils.isNotEmpty(errorInfo)) {
            renderJSON("{\"errorInfo\":\"" + errorInfo + "\"}");
        } else {
            renderJSON("{\"goodsName\":\"" + ecoupon.goods.shortName + "\",\"faceValue\":" + ecoupon.faceValue
                    + ",\"expireAt\":\"" + DateUtil.dateToString(ecoupon.expireAt, 0) + "\"}");
        }
    }

    /**
     * 验证多个券
     */
    public static void verify(Long shopId, String[] eCouponSns) {
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        Shop shop = Shop.findById(shopId);

        List<String> eCouponResult = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(eCouponSns)) {
            for (String eCouponSn : eCouponSns) {
                ECoupon ecoupon = ECoupon.query(eCouponSn, supplierId);
                String ecouponStatusDescription = ECoupon.getECouponStatusDescription(ecoupon, shopId);
                if (StringUtils.isNotEmpty(ecouponStatusDescription)) {
                    eCouponResult.add(ecouponStatusDescription);
                    continue;
                }
                if (ecoupon.status == ECouponStatus.UNCONSUMED) {
                    if (!ecoupon.consumeAndPayCommission(shopId, SupplierRbac.currentUser(), VerifyCouponType.SHOP)) {
                        eCouponResult.add("第三方" + ecoupon.partner + "券验证失败！请确认券状态(是否过期或退款等)！");
                        continue;
                    }

                    sendVerifySMS(ecoupon, shop.name);
                    eCouponResult.add("消费成功.");
                }
            }
        }

        renderJSON(eCouponResult);
    }

    /**
     * 获取最近验证过的n个券号.
     */
    public static void showVerifiedCoupons() {
        SupplierUser supplierUser = SupplierRbac.currentUser();
        List<String> verifiedCoupons = ECoupon.getRecentVerified(supplierUser, 5);
        renderJSON(verifiedCoupons);
    }

    @ActiveNavigation("coupons_verify")
    public static void refresh() {

    }

    private static void sendVerifySMS(ECoupon eCoupon, String shopName) {
        String dateTime = DateUtil.getNowTime();
        String ecouponSNLast4Code = eCoupon.getLastCode(4);
        // 发给消费者
        SMSUtil.send2("【一百券】您尾号" + ecouponSNLast4Code + "的券号于" + dateTime
                + "已成功消费，使用门店：" + shopName + "。如有疑问请致电：400-6262-166", eCoupon.orderItems.phone, eCoupon.replyCode);
    }
}