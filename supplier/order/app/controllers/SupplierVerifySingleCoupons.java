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
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import util.transaction.RemoteRecallCheck;
import util.transaction.TransactionCallback;
import util.transaction.TransactionRetry;

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

    @Before(priority = 1000)
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
        Shop shop = Shop.findById(shopId);

        eCouponSn = StringUtils.trim(eCouponSn);
        if (StringUtils.isNotBlank(eCouponSn)) {
            //根据页面录入券号查询对应信息
            ECoupon ecoupon = ECoupon.query(eCouponSn, supplierId);

            //check券和门店
            checkCoupon(ecoupon, shopId, shopList, supplierUser);

            String ecouponStatusDescription = ECoupon.getECouponStatusDescription(ecoupon, shopId);

            render("SupplierVerifySingleCoupons/index.html", ecoupon, shop, supplierUser, shopList, ecouponStatusDescription);
        }
        render("SupplierVerifySingleCoupons/index.html", shop, supplierUser, shopList);
    }

    /**
     * 验证券
     */
    public static void singleVerify() {
        final Long supplierUserId = SupplierRbac.currentUser().id;
        final Long supplierId = SupplierRbac.currentUser().supplier.id;
        final Long shopId = Long.valueOf(request.params.get("shopId"));
        final String eCouponSn = request.params.get("eCouponSn");

        RemoteRecallCheck.setId("COUPON_" + eCouponSn);
        Boolean success = TransactionRetry.run(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction() {
                return doSingleVerify(supplierUserId, supplierId, shopId, eCouponSn);
            }
        });
        RemoteRecallCheck.cleanUp();

        if (success != null && success) {
            String dateTime = DateUtil.getNowTime();
            Shop shop = Shop.findById(shopId);
            ECoupon ecoupon = ECoupon.query(eCouponSn, supplierId);
            String ecouponSNLast4Code = ecoupon.getLastCode(4);
            // 发给消费者
            SMSUtil.send2("您尾号" + ecouponSNLast4Code + "的券号于" + dateTime
                    + "已成功消费，使用门店：" + shop.name + "。如有疑问请致电：400-6262-166", ecoupon.orderItems.phone, ecoupon.replyCode);
            // 标识为验证成功
            renderArgs.put("success_info", "true");
        }

        render("SupplierVerifySingleCoupons/index.html");
    }

    /**
     * 执行验证，如果失败返回FALSE
     *
     * @param supplierUserId
     * @param supplierId
     * @param shopId
     * @param eCouponSn
     * @return
     */
    private static Boolean doSingleVerify(Long supplierUserId, Long supplierId, Long shopId, String eCouponSn) {
        SupplierUser supplierUser = SupplierUser.findById(supplierUserId);
        List<Shop> shopList = Shop.findShopBySupplier(supplierId);
        ECoupon ecoupon = ECoupon.query(eCouponSn, supplierId);
        //check券和门店
        checkCoupon(ecoupon, shopId, shopList, supplierUser);

        if (StringUtils.isBlank(eCouponSn)) {
            Validation.addError("error-info", "券号不能为空！");
        }

        String ecouponStatusDescription = ECoupon.getECouponStatusDescription(ecoupon, shopId);
        if (ecouponStatusDescription != null) {
            Validation.addError("error-info", ecouponStatusDescription);
        }

        Shop shop = Shop.findById(shopId);

        renderArgs.put("shop", shop);
        renderArgs.put("ecoupon", ecoupon);
        renderArgs.put("supplierUser", supplierUser);
        renderArgs.put("shopList", shopList);
        if (Validation.hasErrors()) {
            return Boolean.FALSE;
        }

        if (ecoupon.status == ECouponStatus.UNCONSUMED) {
            if (!ecoupon.consumeAndPayCommission(shopId, SupplierRbac.currentUser(), VerifyCouponType.SHOP)) {
                Validation.addError("error-info", "第三方" + ecoupon.partner + "券验证失败！请确认券状态(是否过期或退款等)！");
            }
            if (Validation.hasErrors()) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
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
