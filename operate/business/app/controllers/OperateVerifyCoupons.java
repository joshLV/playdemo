package controllers;

import com.uhuila.common.util.DateUtil;
import models.admin.OperateUser;
import models.dangdang.DDAPIInvokeException;
import models.dangdang.DDAPIUtil;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.VerifyCouponType;
import models.sales.Shop;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import play.Logger;
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
        System.out.println("gggggggg        eCoupon.id:" + eCoupon.id);

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
            System.out.println("begin dangdang invoke!!!!!!!!!!!!!!!!!");
            //判断是否当当订单产生的券
            try {
                if (DDAPIUtil.isRefund(eCoupon)) {//如果券在当当上已经退款，则不允许券的消费。
                    renderJSON("4");
                }
                System.out.println("dangdang invoke ok!!!!!!!!!!!!!!!!!");
            } catch (DDAPIInvokeException e) {
                //当当接口调用失败，目前仅记录日志。不阻止券的消费。以便保证用户体验。
                Logger.error(e.getMessage(), e);
            }

            eCoupon.consumeAndPayCommission(shopId, OperateRbac.currentUser().id, null, VerifyCouponType.OP_VERIFY);

            //通知当当该券已经使用
            try {
                DDAPIUtil.notifyVerified(eCoupon);
            } catch (DDAPIInvokeException e) {
                //当当接口调用失败，目前仅记录日志。不阻止券的消费。以便保证用户体验。
                Logger.error(e.getMessage(), e);
            }

            // 发给消费者
            String dateTime = DateUtil.getNowTime();
            String coupon = eCoupon.getLastCode(4);
            SMSUtil.send("【一百券】您尾号" + coupon + "券于" + dateTime
                    + "成功消费，门店：" + shopName + "。客服4006262166", eCoupon.orderItems.phone, eCoupon.replyCode);
        } else {
            renderJSON(eCoupon.status);
        }

        renderJSON("0");
    }
}
