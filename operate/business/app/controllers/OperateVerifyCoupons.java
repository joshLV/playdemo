package controllers;

import com.uhuila.common.util.DateUtil;
import models.operator.OperateUser;
import models.order.CouponsCondition;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.VerifyCouponType;
import models.sales.Shop;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;
import util.transaction.RemoteRecallCheck;
import util.transaction.TransactionCallback;
import util.transaction.TransactionRetry;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-7-30
 * Time: 下午3:37
 */
@With(OperateRbac.class)
@ActiveNavigation("verify_index")
public class OperateVerifyCoupons extends Controller {
    public static int PAGE_SIZE = 15;

    /**
     * 验证页面
     */
    @ActiveNavigation("verify_index")
    public static void index() {
        Long supplierId = null;
        List<Supplier> supplierList = Supplier.findUnDeleted();
        if (supplierList.size() > 0) {
            supplierId = supplierList.get(0).id;
        }

        List shopList = Shop.findShopBySupplier(supplierId);
        if (shopList.size() == 0) {
            renderArgs.put("noShop", "该商户没有添加门店信息！");
        }
        render(shopList, supplierList);

    }

    /**
     * 查询
     *
     * @param eCouponSn 券号
     */
    public static void verify(Long supplierId, Long shopId, String eCouponSn) {
        List<Shop> shopList = Shop.findShopBySupplier(supplierId);
        List<Supplier> supplierList = Supplier.findUnDeleted();
        //根据页面录入券号查询对应信息
        ECoupon ecoupon = null;
        renderArgs.put("supplierList", supplierList);
        renderArgs.put("supplierId", supplierId);
        if (StringUtils.isBlank(eCouponSn)) {
            Validation.addError("error-info", "对不起，券号非法！");
        } else {
            ecoupon = ECoupon.query(eCouponSn, supplierId);
        }

        //check券和门店
        checkCoupon(ecoupon, shopId, supplierId, shopList);

        String ecouponStatusDescription = ECoupon.getECouponStatusDescription(ecoupon, shopId);

        if (ecoupon != null && ecoupon.operateUserId != null) {
            OperateUser operateUser = OperateUser.findById(ecoupon.operateUserId);
            ecoupon.operateUserName = operateUser.userName;
        }
        Shop shop = Shop.findById(shopId);
        renderArgs.put("shop", shop);
        render("/OperateVerifyCoupons/index.html", shopList, supplierId, ecoupon, ecouponStatusDescription);
    }

    private static void checkCoupon(ECoupon ecoupon, Long shopId, Long supplierId, List<Shop> shopList) {
        if (ecoupon == null) {
            Validation.addError("error-info", "对不起，没有该券的信息！");
        }
        if (shopId == null) {
            Validation.addError("error-info", "对不起，该券不能在此门店使用!");
        }

        if (Validation.hasErrors()) {
            render("OperateVerifyCoupons/index.html", ecoupon, supplierId, shopList);
        }
    }

    /**
     * 修改券状态,并产生消费交易记录
     *
     * @param eCouponSn 券号
     */
    public static void update(final Long shopId, final Long supplierId, final String eCouponSn, final Date consumedAt, final String remark) {
        List<Supplier> supplierList = Supplier.findUnDeleted();
        final List<Shop> shopList = Shop.findShopBySupplier(supplierId);

        renderArgs.put("supplierList", supplierList);
        renderArgs.put("supplierId", supplierId);
        renderArgs.put("shopList", shopList);

        // 设置RemoteRecallCheck所使用的标识ID，下次调用时不会再重试.
        RemoteRecallCheck.setId("COUPON_" + eCouponSn);
        Boolean result = TransactionRetry.run(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction() {
                return doUpdateVerify(shopId, supplierId, eCouponSn, consumedAt, remark, shopList);
            }
        });
        RemoteRecallCheck.cleanUp();

        if (result != null && result) {
            renderArgs.put("success_info", "true");
            // 成功验证券，发短信给消费者
            Shop shop = Shop.findById(shopId);
            ECoupon ecoupon = ECoupon.query(eCouponSn, supplierId);
            String dateTime = DateUtil.getNowTime();
            String coupon = ecoupon.getLastCode(4);
            SMSUtil.send("您尾号" + coupon + "券于" + dateTime
                    + "成功消费，门店：" + shop.name + "。客服4006262166", ecoupon.orderItems.phone, ecoupon.replyCode);
        }
        render("OperateVerifyCoupons/index.html");
    }

    private static Boolean doUpdateVerify(Long shopId, Long supplierId, String eCouponSn, Date consumedAt, String remark, List<Shop> shopList) {
        if (StringUtils.isBlank(eCouponSn)) {
            Validation.addError("error-info", "券号非法！");
            return Boolean.FALSE;
        }
        ECoupon ecoupon = ECoupon.query(eCouponSn, supplierId);
        //check券和门店
        checkCoupon(ecoupon, shopId, supplierId, shopList);

        String ecouponStatusDescription = ECoupon.getECouponStatusDescription(ecoupon, shopId);
        if (ecouponStatusDescription != null) {
            Validation.addError("error-info", ecouponStatusDescription);
            return Boolean.FALSE;
        }
        Shop shop = Shop.findById(shopId);
        renderArgs.put("shop", shop);
        renderArgs.put("ecoupon", ecoupon);
        if (ecoupon.status == ECouponStatus.UNCONSUMED) {
            String historyRemark = "运营平台代理验证，原因:" + remark;
            if (!ecoupon.consumeAndPayCommission(shopId, OperateRbac.currentUser(), null, VerifyCouponType.OP_VERIFY,
                    ecoupon.eCouponSn, consumedAt, historyRemark)) {
                Validation.addError("error-info", "第三方" + ecoupon.partner + "券验证失败！请确认券状态(是否过期或退款等)！");
            }
            if (Validation.hasErrors()) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
        return Boolean.FALSE; //这里不应该出现
    }

    /**
     * 虚拟验证页面
     */
    @ActiveNavigation("virtual_verify_index")
    public static void virtual(CouponsCondition condition) {
        if (condition == null) {
            condition = new CouponsCondition();
        }
        List<ECoupon> couponList = ECoupon.findVirtualCoupons(condition);
        BigDecimal totalSalePrice = calculateSalePrice(couponList);
        render(couponList, condition, totalSalePrice);

    }

    private static BigDecimal calculateSalePrice(List<ECoupon> couponList) {
        BigDecimal totalSalePrice = BigDecimal.ZERO;
        for (ECoupon coupon : couponList) {
            totalSalePrice = totalSalePrice.add(coupon.salePrice);
        }
        return totalSalePrice;
    }

    /**
     * 验证
     *
     * @param id
     */
    @ActiveNavigation("virtual_verify_index")
    public static void virtualVerify(Long id, CouponsCondition condition) {
        if (condition == null) {
            condition = new CouponsCondition();
        }
        ECoupon ecoupon = ECoupon.findById(id);
        String ecouponStatusDescription = ECoupon.checkOtherECouponInfo(ecoupon);
        if (ecouponStatusDescription != null) {
            renderText(ecouponStatusDescription);
        }

        boolean verifyFlag = false;
        if (ecoupon.status == ECouponStatus.UNCONSUMED) {
            verifyFlag = ecoupon.virtualVerify(OperateRbac.currentUser().id);
        }
        if (!verifyFlag) {
            Validation.addError("verify-error-info", "虚拟验证失败！");
        }
        List<ECoupon> couponList = ECoupon.findVirtualCoupons(condition);
        BigDecimal totalSalePrice = calculateSalePrice(couponList);
        if (Validation.hasErrors()) {
            render("OperateVerifyCoupons/virtual.html", couponList, id, condition, totalSalePrice);
        }

        render("OperateVerifyCoupons/virtual.html", couponList, condition, totalSalePrice);
    }
}
