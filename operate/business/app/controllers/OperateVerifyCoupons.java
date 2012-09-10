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
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

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

        List shopList = Shop.findShopBySupplier(supplierId);
        if (Validation.hasErrors()) {
            render("/OperateVerifyCoupons/index.html", shopList, eCouponSn, supplierId);
        }

        //根据页面录入券号查询对应信息
        ECoupon ecoupon = ECoupon.query(eCouponSn, supplierId);
        if (ecoupon != null && ecoupon.operateUserId != null) {
            OperateUser operateUser = OperateUser.findById(ecoupon.operateUserId);
            ecoupon.operateUserName = operateUser.userName;
        }
        render("/OperateVerifyCoupons/verify.html", shopList, shopId, ecoupon);
    }

    /**
     * 修改券状态,并产生消费交易记录
     *
     * @param eCouponSn 券号
     */
    @ActiveNavigation("coupons_verify")
    public static void update(Long shopId, Long supplierId, String eCouponSn, String shopName) {
        List shopList = Shop.findShopBySupplier(supplierId);
        if (Validation.hasErrors()) {
            render("/OperateVerifyCoupons/index.html", shopList, eCouponSn, supplierId);
        }

        ECoupon eCoupon = ECoupon.query(eCouponSn, supplierId);
        //根据页面录入券号查询对应信息,并产生消费交易记录
        if (eCoupon == null) {
            renderJSON("err");
        }

        if (eCoupon.status == ECouponStatus.UNCONSUMED) {
            //冻结的券
            if (eCoupon.isFreeze == 1) {
                renderJSON("3");
            }
            if (!eCoupon.isBelongShop(shopId)) {
                renderJSON("1");
            }
            //不再验证时间范围内
            if (!eCoupon.checkVerifyTimeRegion(new Date())) {
                String info = eCoupon.getCheckInfo();
                renderJSON("{\"error\":\"2\",\"info\":\"" + info + "\"}");
            }
            eCoupon.consumeAndPayCommission(shopId, OperateRbac.currentUser().id, null, VerifyCouponType.OP_VERIFY);
            String dateTime = DateUtil.getNowTime();
            String coupon = eCoupon.getLastCode(4);

            // 发给消费者
            SMSUtil.send("【券市场】您尾号" + coupon + "券于" + dateTime
                    + "成功消费，门店：" + shopName + "。客服4006262166", eCoupon.orderItems.phone, eCoupon.replyCode);
        } else {
            renderJSON(eCoupon.status);
        }

        renderJSON("0");
    }
}
