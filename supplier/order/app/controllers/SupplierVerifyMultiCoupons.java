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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p/>
 * User: yanjy
 * Date: 12-11-29
 * Time: 上午10:43
 */
@With(SupplierRbac.class)
@ActiveNavigation("coupons_multi_index")
public class SupplierVerifyMultiCoupons extends Controller {

    @Before(priority=1000)
    public static void storeShopIp() {
        SupplierUser supplierUser = SupplierRbac.currentUser();
        String strShopId = request.params.get("shopId");
        System.out.println("hello storeshopid=" + strShopId);
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
    @ActiveNavigation("coupons_multi_index")
    public static void multiQuery(Long shopId, String eCouponSn) {
        String amount = StringUtils.trimToEmpty(request.params.get("amount"));
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        Long supplierUserId = SupplierRbac.currentUser().id;
        SupplierUser supplierUser = SupplierUser.findById(supplierUserId);
        List shopList = Shop.findShopBySupplier(supplierId);
        //根据页面录入券号查询对应信息
        ECoupon ecoupon = ECoupon.query(eCouponSn, supplierId);
        if (ecoupon == null) {
            Validation.addError("error-info", "对不起，没有该券的信息！");
        }
        if (shopId == null) {
            Validation.addError("error-info", "对不起，该券不能在此门店使用!");
        }
        Shop shop = Shop.findById(shopId);
        if (Validation.hasErrors()) {
            render("SupplierVerifyMultiCoupons/index.html", ecoupon, shop, supplierUser, shopList);
        }
        String ecouponStatusDescription = ECoupon.getECouponStatusDescription(ecoupon, shopId);
        List<ECoupon> ecoupons = ECoupon.queryUnconsumedCouponsWithSameGoodsGroups(ecoupon);

        BigDecimal verifyAmount = summaryECouponsAmount(ecoupons);
        renderArgs.put("amount", amount);
        renderArgs.put("verifyAmount", verifyAmount);

        render("SupplierVerifyMultiCoupons/index.html", ecoupon, ecoupons, ecouponStatusDescription, shop, supplierUser, shopList);
    }

    private static BigDecimal summaryECouponsAmount(List<ECoupon> ecoupons) {
        BigDecimal amount = BigDecimal.ZERO;
        for (ECoupon ecoupon : ecoupons) {
            amount = amount.add(ecoupon.faceValue);
        }
        return amount;
    }

    /**
     * 验证多个券
     */
    public static void multiVerify() {
        Long supplierUserId = SupplierRbac.currentUser().id;
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        Long shopId = Long.valueOf(request.params.get("shopId"));
        String amount = StringUtils.trimToEmpty(request.params.get("verifyAmount"));
        String eCouponSn = request.params.get("eCouponSn");
        SupplierUser supplierUser = SupplierUser.findById(supplierUserId);
        List shopList = Shop.findShopBySupplier(supplierId);

        ECoupon ecoupon = ECoupon.query(eCouponSn, supplierId);
        if (ecoupon == null) {
            Validation.addError("error-info", "对不起，没有该券的信息！");
        }
        if (shopId == null) {
            Validation.addError("error-info", "对不起，该券不能在此门店使用!");
        }
        Shop shop = Shop.findById(shopId);
        if (StringUtils.isEmpty(amount) || new BigDecimal(amount).compareTo(BigDecimal.ZERO) < 0) {
            Validation.addError("error-info", "请输入正确的验证金额！");
        }

        String ecouponStatusDescription = ECoupon.getECouponStatusDescription(ecoupon, shopId);
        if (ecouponStatusDescription != null) {
            Validation.addError("error-info", ecouponStatusDescription);
        }
        if (Validation.hasErrors()) {
            render("SupplierVerifyMultiCoupons/index.html", ecouponStatusDescription, shop, supplierUser, shopList);
        }

        if (ecoupon.status == ECouponStatus.UNCONSUMED) {
            String ecouponSNLast4Code = ecoupon.getLastCode(4);
            BigDecimal verifyAmount = new BigDecimal(amount);
            // 多张券验证
            List<ECoupon> ecoupons = ECoupon.queryUnconsumedCouponsWithSameGoodsGroups(ecoupon);
            renderArgs.put("ecoupons", ecoupons);
            List<ECoupon> checkECoupons = ECoupon.selectCheckECoupons(verifyAmount, ecoupons, ecoupon);
            BigDecimal consumedAmount = BigDecimal.ZERO;

            int checkedCount = 0;
            List<ECoupon> realCheckECoupon = new ArrayList<>();  //可能验证失败，所以要有一个实际真正验证成功的ecoupons
            for (ECoupon e : checkECoupons) {
                if (e.consumeAndPayCommission(shopId, SupplierRbac.currentUser(), VerifyCouponType.SHOP)) {
                    checkedCount += 1;
                    consumedAmount = consumedAmount.add(e.faceValue);
                    realCheckECoupon.add(e);
                } else {
                    Validation.addError("error-info", "第三方" + ecoupon.partner + "券验证失败！券号：" + ecoupon.eCouponSn + ",请确认券状态！");
                }
            }
            if (consumedAmount.compareTo(BigDecimal.ZERO) == 0) {
                // 没有验证到券
                Validation.addError("error-info", "没有验证任何券，可能是输入金额小于最小面值的券！");
            }
            renderArgs.put("consumedAmount", consumedAmount);
            if (Validation.hasErrors()) {
                render("SupplierVerifyMultiCoupons/index.html", shop, ecoupon, supplierUser, shopList);
            }

            List<ECoupon> availableECoupons = substractECouponList(ecoupons, realCheckECoupon);
            BigDecimal availableAmount = summaryECouponsAmount(availableECoupons);
            List<String> availableECouponSNs = new ArrayList<>();
            for (ECoupon ae : availableECoupons) {
                availableECouponSNs.add(ae.eCouponSn);
            }
            if (availableECoupons.size() > 0) {
                SMSUtil.send2("【一百券】您尾号" + ecouponSNLast4Code
                        + "共" + checkedCount + "张券(总面值" + consumedAmount.setScale(0) + "元)于"
                        + DateUtil.getNowTime() + "已成功消费，使用门店：" + shop.name + "。您还有" + availableECouponSNs.size() + "张券（"
                        + StringUtils.join(availableECouponSNs, "/")
                        + "总面值" + availableAmount.setScale(0) + "元）未消费。如有疑问请致电：4006262166",
                        ecoupon.orderItems.phone, ecoupon.replyCode);
            } else {
                SMSUtil.send2("【一百券】您尾号" + ecouponSNLast4Code
                        + "共" + checkedCount + "张券(总面值" + consumedAmount.setScale(0) + "元)于"
                        + DateUtil.getNowTime() + "已成功消费，使用门店：" + shop.name + "。如有疑问请致电：4006262166",
                        ecoupon.orderItems.phone, ecoupon.replyCode);
            }

            renderArgs.put("success_info", true);
            if (verifyAmount.compareTo(consumedAmount) == 0) {
                renderArgs.put("info", "成功验证：" + consumedAmount + "元!");
            } else {
                renderArgs.put("info", "成功验证：" + consumedAmount + "元，顾客还需要现金支付：" + verifyAmount.subtract(consumedAmount) + "元！");
            }
        }

        render("SupplierVerifyMultiCoupons/index.html", shop, amount, ecoupon, supplierUser, shopList);

    }

    /**
     * 得到sourceECoupons - checkECoupons的数组.
     *
     * @param sourceECoupons
     * @param checkECoupons
     * @return
     */
    private static List<ECoupon> substractECouponList(List<ECoupon> sourceECoupons,
                                                      List<ECoupon> checkECoupons) {
        Set<Long> checkECouponIdSet = new HashSet<>();
        for (ECoupon e : checkECoupons) {
            checkECouponIdSet.add(e.id);
        }
        List<ECoupon> results = new ArrayList<>();
        for (ECoupon e : sourceECoupons) {
            if (!checkECouponIdSet.contains(e.id)) {
                results.add(e);
            }
        }
        return results;
    }

}
