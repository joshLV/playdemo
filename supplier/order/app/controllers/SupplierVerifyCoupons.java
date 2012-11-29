package controllers;

import com.uhuila.common.util.DateUtil;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponPartner;
import models.order.ECouponStatus;
import models.order.VerifyCouponType;
import models.sales.Shop;
import models.sms.SMSUtil;
import navigation.annotations.ActiveNavigation;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;
import org.apache.commons.lang.StringUtils;

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
public class SupplierVerifyCoupons extends Controller {

    /**
     * 券验证页面
     */
    @ActiveNavigation("coupons_single_verify")
    public static void single() {
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
    public static void query(Long shopId, String eCouponSn, boolean isSingle) {
        Long supplierId = SupplierRbac.currentUser().supplier.id;

        //根据页面录入券号查询对应信息
        ECoupon ecoupon = ECoupon.query(eCouponSn, supplierId);

        String ecouponStatusDescription = ECoupon.getECouponStatusDescription(ecoupon, shopId);
        List<ECoupon> ecoupons = null;
        if (isSingle) {
            render("/SupplierVerifyCoupons/consume.html", ecoupon, isSingle, ecoupons, ecouponStatusDescription);
        }
        ecoupons = ECoupon.queryUnconsumedCouponsWithSameGoodsGroups(ecoupon);

        BigDecimal amount = summaryECouponsAmount(ecoupons);

        renderArgs.put("amount", amount);
        render("/SupplierVerifyCoupons/consume.html", ecoupon, ecoupons, amount, ecouponStatusDescription);
    }

    private static BigDecimal summaryECouponsAmount(List<ECoupon> ecoupons) {
        BigDecimal amount = BigDecimal.ZERO;
        for (ECoupon ecoupon : ecoupons) {
            amount = amount.add(ecoupon.faceValue);
        }
        return amount;
    }

    @ActiveNavigation("coupons_single_verify")
    public static void singleVerify() {
        Long supplierUserId = SupplierRbac.currentUser().id;
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        Long shopId = Long.valueOf(request.params.get("shopId"));
        String eCouponSn = request.params.get("eCouponSn");
        SupplierUser supplierUser = SupplierUser.findById(supplierUserId);
        List shopList = Shop.findShopBySupplier(supplierId);

        ECoupon eCoupon = ECoupon.query(eCouponSn, supplierId);
        String ecouponStatusDescription = ECoupon.getECouponStatusDescription(eCoupon, shopId);
        if (ecouponStatusDescription != null) {
            Validation.addError("error-info", ecouponStatusDescription);
        }
        if (Validation.hasErrors()) {
            render("SupplierVerifyCoupons/single.html", shopId, eCouponSn, eCoupon, supplierUser, shopList);
        }

        Shop shop = Shop.findById(shopId);
        eCoupon.partner = ECouponPartner.DD;
        if (eCoupon.status == ECouponStatus.UNCONSUMED) {
            if (!eCoupon.consumeAndPayCommission(shopId, null, SupplierRbac.currentUser(), VerifyCouponType.SHOP)) {
                Validation.addError("error-info", "第三方" + eCoupon.partner + "券验证失败！券号：" + eCoupon.eCouponSn);
            }
            if (Validation.hasErrors()) {
                render("SupplierVerifyCoupons/single.html", shopId, eCouponSn, eCoupon, supplierUser, shopList);
            }
            String dateTime = DateUtil.getNowTime();
            String ecouponSNLast4Code = eCoupon.getLastCode(4);
            // 发给消费者
            SMSUtil.send2("【一百券】您尾号" + ecouponSNLast4Code + "的券号于" + dateTime
                    + "已成功消费，使用门店：" + shop.name + "。如有疑问请致电：400-6262-166", eCoupon.orderItems.phone, eCoupon.replyCode);
        }

        renderArgs.put("success_info", "券消费成功！");
        render("SupplierVerifyCoupons/single.html", shopId, eCouponSn, eCoupon, supplierUser, shopList);

    }

    @ActiveNavigation("coupons_multi_verify")
    public static void multi() {
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

    @ActiveNavigation("coupons_multi_verify")
    public static void multiVerify() {
        Long supplierUserId = SupplierRbac.currentUser().id;
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        Long shopId = Long.valueOf(request.params.get("shopId"));
        String amount = StringUtils.trimToEmpty(request.params.get("verifyAmount"));
        String eCouponSn = request.params.get("eCouponSn");
        SupplierUser supplierUser = SupplierUser.findById(supplierUserId);
        List shopList = Shop.findShopBySupplier(supplierId);

        ECoupon eCoupon = ECoupon.query(eCouponSn, supplierId);
        String ecouponStatusDescription = ECoupon.getECouponStatusDescription(eCoupon, shopId);
        if (ecouponStatusDescription != null) {
            Validation.addError("error-info", ecouponStatusDescription);
        }
        if (StringUtils.isEmpty(amount) || new BigDecimal(amount).compareTo(BigDecimal.ZERO) < 0) {
            Validation.addError("error-info", "请输入正确的验证金额！");
        }
        if (Validation.hasErrors()) {
            render("SupplierVerifyCoupons/multi.html", shopId, eCouponSn, eCoupon, supplierUser, shopList);
        }
        Shop shop = Shop.findById(shopId);
        if (eCoupon.status == ECouponStatus.UNCONSUMED) {
            String ecouponSNLast4Code = eCoupon.getLastCode(4);
            BigDecimal verifyAmount = new BigDecimal(amount);
            // 多张券验证
            List<ECoupon> ecoupons = ECoupon.queryUnconsumedCouponsWithSameGoodsGroups(eCoupon);
            renderArgs.put("ecoupons", ecoupons);
            List<ECoupon> checkECoupons = ECoupon.selectCheckECoupons(verifyAmount, ecoupons);
            BigDecimal consumedAmount = BigDecimal.ZERO;

            int checkedCount = 0;
            List<ECoupon> realCheckECoupon = new ArrayList<>();  //可能验证失败，所以要有一个实际真正验证成功的ecoupons
            for (ECoupon e : checkECoupons) {
                if (e.consumeAndPayCommission(shopId, null, SupplierRbac.currentUser(),
                        VerifyCouponType.SHOP, e.eCouponSn)) {
                    checkedCount += 1;
                    consumedAmount = consumedAmount.add(e.faceValue);
                    realCheckECoupon.add(e);
                } else {
                    Validation.addError("error-info", "第三方" + e.partner + "券验证失败！券号：" + e.eCouponSn);
                }
            }
            if (consumedAmount.compareTo(BigDecimal.ZERO) == 0) {
                // 没有验证到券
                Validation.addError("error-info", "没有验证任何券，可能是输入金额小于最小面值的券！");
            }
            if (Validation.hasErrors()) {
                render("SupplierVerifyCoupons/multi.html", shopId, eCouponSn, eCoupon, supplierUser, shopList);
            }
            renderArgs.put("consumedAmount", consumedAmount);

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
                        eCoupon.orderItems.phone, eCoupon.replyCode);
            } else {
                SMSUtil.send2("【一百券】您尾号" + ecouponSNLast4Code
                        + "共" + checkedCount + "张券(总面值" + consumedAmount.setScale(0) + "元)于"
                        + DateUtil.getNowTime() + "已成功消费，使用门店：" + shop.name + "。如有疑问请致电：4006262166",
                        eCoupon.orderItems.phone, eCoupon.replyCode);
            }

            if (verifyAmount.compareTo(consumedAmount) == 0) {
                renderArgs.put("success_info", "成功验证" + consumedAmount + "元!");
            } else {
                renderArgs.put("success_info", "成功验证" + consumedAmount + "元，顾客还需要现金支付" + verifyAmount.subtract(consumedAmount));
            }
        }

        render("SupplierVerifyCoupons/multi.html", shopId, eCouponSn, eCoupon, supplierUser, shopList);

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
