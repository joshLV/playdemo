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
     * 券验证页面
     */
    public static void multiQuery() {
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
        String errorInfo = ECoupon.getECouponStatusDescription(ecoupon, shopId);
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
        Supplier supplier = Supplier.findById(supplierId);
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

        String successInfo = "消费成功！ " + supplier.getName() + "(" + ecoupon.faceValue + ")";
        render("SupplierVerifyECoupons/index.html", shop, ecoupon, supplierUser, shopList, successInfo);
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
            render("SupplierVerifyECoupons/index.html", ecoupon, shop, supplierUser, shopList);
        }
        String ecouponStatusDescription = ECoupon.getECouponStatusDescription(ecoupon, shopId);
        List<ECoupon> ecoupons = ECoupon.queryUnconsumedCouponsWithSameGoodsGroups(ecoupon);

        BigDecimal verifyAmount = summaryECouponsAmount(ecoupons);
        renderArgs.put("amount", amount);
        renderArgs.put("verifyAmount", verifyAmount);

        render("SupplierVerifyECoupons/index.html", ecoupon, ecoupons, ecouponStatusDescription, shop, supplierUser, shopList);
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
            render("SupplierVerifyECoupons/index.html", ecouponStatusDescription, shop, supplierUser, shopList);
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
                if (e.consumeAndPayCommission(shopId, null, SupplierRbac.currentUser(),
                        VerifyCouponType.SHOP, e.eCouponSn)) {
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
                render("SupplierVerifyECoupons/index.html", shop, ecoupon, supplierUser, shopList);
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

        render("SupplierVerifyECoupons/index.html", shop, amount, ecoupon, supplierUser, shopList);

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