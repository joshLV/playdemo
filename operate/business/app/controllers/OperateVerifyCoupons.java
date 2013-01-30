package controllers;

import com.uhuila.common.util.DateUtil;
import models.admin.OperateUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.VerifyCouponType;
import models.sales.Shop;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import play.data.binding.As;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

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
        ECoupon ecoupon = ECoupon.query(eCouponSn, supplierId);
        renderArgs.put("supplierList", supplierList);
        renderArgs.put("supplierId", supplierId);

        //check券和门店
        checkCoupon(ecoupon, shopId, supplierId, shopList);

        String ecouponStatusDescription = ECoupon.getECouponStatusDescription(ecoupon, shopId);
        System.out.println("ecouponStatusDescription:" + ecouponStatusDescription);


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

        Shop shop = Shop.findById(shopId);
        if (Validation.hasErrors()) {
            render("OperateVerifyCoupons/index.html", shop, ecoupon, supplierId, shopList);
        }
    }

    /**
     * 修改券状态,并产生消费交易记录
     *
     * @param eCouponSn 券号
     */
    public static void update(Long shopId, Long supplierId, String eCouponSn) {
        List<Supplier> supplierList = Supplier.findUnDeleted();
        List<Shop> shopList = Shop.findShopBySupplier(supplierId);
        ECoupon ecoupon = ECoupon.query(eCouponSn, supplierId);
        renderArgs.put("supplierList", supplierList);
        renderArgs.put("supplierId", supplierId);
        //check券和门店
        checkCoupon(ecoupon, shopId, supplierId, shopList);

        String ecouponStatusDescription = ECoupon.getECouponStatusDescription(ecoupon, shopId);
        if (ecouponStatusDescription != null) {
            Validation.addError("error-info", ecouponStatusDescription);
        }
        Shop shop = Shop.findById(shopId);
        if (ecoupon.status == ECouponStatus.UNCONSUMED) {
            if (!ecoupon.consumeAndPayCommission(shopId, OperateRbac.currentUser().id, null, VerifyCouponType.OP_VERIFY)) {
                Validation.addError("error-info", "第三方" + ecoupon.partner + "券验证失败！请确认券状态(是否过期或退款等)！");
            }
            if (Validation.hasErrors()) {
                render("OperateVerifyCoupons/index.html", shop, ecoupon, shopList);
            }
            // 发给消费者
            String dateTime = DateUtil.getNowTime();
            String coupon = ecoupon.getLastCode(4);
            SMSUtil.send("【一百券】您尾号" + coupon + "券于" + dateTime
                    + "成功消费，门店：" + shop.name + "。客服4006262166", ecoupon.orderItems.phone, ecoupon.replyCode);
        }
        renderArgs.put("success_info", "true");
        render("OperateVerifyCoupons/index.html", shop, ecoupon, shopList);
    }

    /**
     * 虚拟验证页面
     */
    @ActiveNavigation("virtual_verify_index")
    public static void virtual() {
        List<ECoupon> couponList = ECoupon.findVirtualCoupons();
        render(couponList);

    }

    public static void virtualCoupons(@As List<ECoupon> coupons) {
        System.out.println(coupons + ">>>>coupons");
        List<ECoupon> couponList = ECoupon.findVirtualCoupons();
        render("OperateVerifyCoupons/virtual.html", couponList);
    }

    public static void virtualVerify() {
        render();
    }
}
