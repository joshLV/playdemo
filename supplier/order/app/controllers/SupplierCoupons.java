package controllers;

import com.uhuila.common.util.DateUtil;
import models.admin.SupplierUser;
import models.order.CouponsCondition;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.VerifyCouponType;
import models.sales.Shop;
import models.sms.SMSUtil;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@With(SupplierRbac.class)
public class SupplierCoupons extends Controller {

    public static int PAGE_SIZE = 15;

    /**
     * 验证页面
     */
    @ActiveNavigation("coupons_verify")
    public static void verify() {
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
    @ActiveNavigation("coupons_verify")
    public static void query(Long shopId, String eCouponSn) {
        if (Validation.hasErrors()) {
            render("../views/SupplierCoupons/index.html", eCouponSn);
        }

        Long supplierId = SupplierRbac.currentUser().supplier.id;

        if (StringUtils.isNotBlank(eCouponSn)) {
            //根据页面录入券号查询对应信息
            ECoupon ecoupon = ECoupon.query(eCouponSn, supplierId);

            String ecouponStatusDescription = ECoupon.getECouponStatusDescription(ecoupon, shopId);

            List<ECoupon> ecoupons = ECoupon.queryUnconsumedCouponsWithSameGoodsGroups(ecoupon);

            BigDecimal amount = summaryECouponsAmount(ecoupons);

            render("/SupplierCoupons/consume.html", ecoupon, ecoupons, amount, ecouponStatusDescription);
        }
    }

    private static BigDecimal summaryECouponsAmount(List<ECoupon> ecoupons) {
        BigDecimal amount = BigDecimal.ZERO;
        for (ECoupon ecoupon : ecoupons) {
            amount = amount.add(ecoupon.faceValue);
        }
        return amount;
    }


    /**
     * 修改券状态,并产生消费交易记录
     *
     * @param eCouponSn 券号
     */
    @ActiveNavigation("coupons_verify")
    public static void update(Long shopId, String eCouponSn, BigDecimal verifyAmount) {
        if (Validation.hasErrors()) {
            render("../views/SupplierCoupons/index.html", eCouponSn);
        }

        Long supplierId = SupplierRbac.currentUser().supplier.id;

        Shop shop = Shop.findById(shopId);
        if (shop == null) {
            renderJSON("{\"code\":\"1\"");
        }
        ECoupon eCoupon = ECoupon.query(eCouponSn, supplierId);
        //根据页面录入券号查询对应信息,并产生消费交易记录
        if (StringUtils.isBlank(eCouponSn) || eCoupon == null) {
            renderJSON("{\"code\":\"err\"");
        }

        if (eCoupon.status == ECouponStatus.UNCONSUMED) {
            //冻结的券
            if (eCoupon.isFreeze == 1) {
                renderJSON("{\"code\":\"3\"}");
            }
            if (!eCoupon.isBelongShop(shopId)) {
                renderJSON("{\"code\":\"1\"}");
            }
            if (eCoupon.isExpired()) {
                renderJSON("{\"code\":\"4\"}");
            }
            //不在验证时间范围内
            if (!eCoupon.checkVerifyTimeRegion(new Date())) {
                String info = eCoupon.getCheckInfo();
                renderJSON("{\"code\":\"2\",\"info\":\"" + info + "\"}");
            }

            String dateTime = DateUtil.getNowTime();
            String ecouponSNLast4Code = eCoupon.getLastCode(4);

            List<ECoupon> ecoupons = ECoupon.queryUnconsumedCouponsWithSameGoodsGroups(eCoupon);
            renderArgs.put("ecoupons", ecoupons);

            // 如果只有一张券.
            if (ecoupons.size() == 1) {
                if (!eCoupon.consumeAndPayCommission(shopId, SupplierRbac.currentUser(), VerifyCouponType.SHOP)) {
                    renderJSON("{\"code\":\"5\"}");
                }
                // 发给消费者
                SMSUtil.send2("您尾号" + ecouponSNLast4Code + "的券号于" + dateTime
                        + "已成功消费，使用门店：" + shop.name + "。如有疑问请致电：4006865151", eCoupon.orderItems.phone,
                        eCoupon.replyCode);
            } else {
                // 多张券验证
                if (verifyAmount == null || verifyAmount.compareTo(BigDecimal.ZERO) < 0) {
                    renderJSON("{\"code\":\"6\"}");
                }
                List<ECoupon> checkECoupons = ECoupon.selectCheckECoupons(verifyAmount, ecoupons, eCoupon);

                BigDecimal consumedAmount = BigDecimal.ZERO;

                int checkedCount = 0;
                List<ECoupon> realCheckECoupon = new ArrayList<>();  //可能验证失败，所以要有一个实际真正验证成功的ecoupons
                for (ECoupon e : checkECoupons) {
                    if (e.consumeAndPayCommission(shopId, SupplierRbac.currentUser(),
                            VerifyCouponType.SHOP, eCoupon.eCouponSn)) {
                        checkedCount += 1;
                        consumedAmount = consumedAmount.add(e.faceValue);
                        realCheckECoupon.add(e);
                    }
                }
                renderArgs.put("consumedAmount", consumedAmount);

                List<ECoupon> availableECoupons = substractECouponList(ecoupons, realCheckECoupon);
                BigDecimal availableAmount = summaryECouponsAmount(availableECoupons);
                List<String> availableECouponSNs = new ArrayList<>();
                for (ECoupon ae : availableECoupons) {
                    availableECouponSNs.add(ae.eCouponSn);
                }


                if (consumedAmount.compareTo(BigDecimal.ZERO) == 0) {
                    // 没有验证到券
                    renderJSON("{\"code\":\"7\",\"info\":\"没有验证任何券，可能是输入金额小于最小面值的券\"}");
                }

                if (availableECoupons.size() > 0) {
                    SMSUtil.send2("您尾号" + ecouponSNLast4Code
                            + "共" + checkedCount + "张券(总面值" + consumedAmount.setScale(2,BigDecimal.ROUND_HALF_UP) + "元)于"
                            + DateUtil.getNowTime() + "已成功消费，使用门店：" + shop.name + "。您还有" + availableECouponSNs.size() + "张券（"
                            + StringUtils.join(availableECouponSNs, "/")
                            + "总面值" + availableAmount.setScale(2,BigDecimal.ROUND_HALF_UP) + "元）未消费。如有疑问请致电：4006865151",
                            eCoupon.orderItems.phone, eCoupon.replyCode);
                } else {
                    SMSUtil.send2("您尾号" + ecouponSNLast4Code
                            + "共" + checkedCount + "张券(总面值" + consumedAmount.setScale(2,BigDecimal.ROUND_HALF_UP) + "元)于"
                            + DateUtil.getNowTime() + "已成功消费，使用门店：" + shop.name + "。如有疑问请致电：4006865151",
                            eCoupon.orderItems.phone, eCoupon.replyCode);
                }

                if (verifyAmount.compareTo(consumedAmount) == 0) {
                    renderJSON("{\"code\":\"7\",\"info\":\"成功验证" + consumedAmount + "元\"}");
                } else {
                    renderJSON("{\"code\":\"7\",\"info\":\"成功验证" + consumedAmount + "元，顾客还需要现金支付" + verifyAmount.subtract(consumedAmount) + "\"}");
                }
            }
        } else {
            renderJSON(eCoupon.status);
        }

        renderJSON("{\"code\":\"0\"}");
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


    /**
     * 券号列表
     */
    @ActiveNavigation("coupons_index")
    public static void index(CouponsCondition condition) {
        if (condition == null) {
            condition = new CouponsCondition();
        }

        condition.supplier = SupplierRbac.currentUser().supplier;

        condition.status = ECouponStatus.CONSUMED;

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        JPAExtPaginator<ECoupon> couponPage = ECoupon.query(condition, pageNumber, PAGE_SIZE);
        render(couponPage, condition);
    }

    public static void couponExcelOut(CouponsCondition condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if (condition == null) {
            condition = new CouponsCondition();
        }
        condition.status = ECouponStatus.CONSUMED;
        condition.supplier = SupplierRbac.currentUser().supplier;
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "券内容列表_" + System.currentTimeMillis() + ".xls");
        JPAExtPaginator<ECoupon> couponPage = ECoupon.query(condition, pageNumber, PAGE_SIZE);
        for (ECoupon coupon : couponPage) {
            String staff = "";
            coupon.verifyTypeInfo = Messages.get("coupon." + coupon.verifyType);
            coupon.statusInfo = Messages.get("coupon." + coupon.status);
            if (coupon.supplierUser != null) {
                if (StringUtils.isNotBlank(coupon.supplierUser.userName))
                    staff = coupon.supplierUser.userName;
                if (StringUtils.isNotBlank(coupon.supplierUser.jobNumber))
                    staff += "(工号:" + coupon.supplierUser.jobNumber + ")";
            }
            coupon.staff = staff;
        }
        render(couponPage, condition);
    }
}
