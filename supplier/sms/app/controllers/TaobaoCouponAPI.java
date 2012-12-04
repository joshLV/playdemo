package controllers;

import com.google.gson.Gson;
import models.order.*;
import models.taobao.TaobaoCouponUtil;
import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Controller;

import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-11-27
 */
public class TaobaoCouponAPI extends Controller {

    public static void index(String method) {
        Map<String, String> params = request.params.allSimple();
        params.remove("body");
        Logger.info("taobao coupon request: \n%s", new Gson().toJson(params));
        if (!TaobaoCouponUtil.verifyParam(params)) {
            Logger.info("taobao coupon request error: param verify failed!");
            renderJSON("{\"code\":501}");
            return;//签名错误
        }
        switch (method) {
            case "send":
                send(params);
                break;
            case "resend":
                resend(params);
                break;
            case "cancel":
                cancel(params);
                break;
            case "modified"://用户修改手机号
                modified(params);
                break;
            case "order_modify"://订单修改通知
                renderJSON("{\"code\":200}");//我们不做任何操作
                break;
            default:
                throw new IllegalArgumentException("no such method");
        }
    }

    /**
     * 接收发码通知
     *
     * @param params 淘宝传过来的参数
     */
    private static void send(Map<String, String> params) {
        Long orderId;
        try {
            orderId = Long.parseLong(params.get("order_id"));//淘宝订单交易号
        } catch (Exception e) {
            Logger.info("taobao coupon request failed: invalid order_id %s", params.get("order_id"));
            renderJSON("{\"code\":502}");
            return;
        }

        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId",
                OuterOrderPartner.TB, orderId).first();
        //如果找不到该orderCode的订单，说明还没有新建，则新建一个
        if (outerOrder == null) {
            outerOrder = new OuterOrder();
            outerOrder.partner = OuterOrderPartner.TB;
            outerOrder.status = OuterOrderStatus.ORDER_COPY;
            outerOrder.message = new Gson().toJson(params);
            outerOrder.orderId = orderId;
            outerOrder.save();
            try { // 将订单写入数据库
                JPA.em().flush();
            } catch (Exception e) { // 如果写入失败，说明 已经存在一个相同的orderCode 的订单，则放弃
                Logger.info("taobao coupon request failed: concurrency request");
                renderJSON("{\"code\":504}");
                return;//解析错误
            }
        }
        Logger.info("taobao coupon request copy success");
        renderJSON("{\"code\":200}");
    }

    private static void resend(Map<String, String> params) {
        Long orderId;
        String sellerNick;
        try {
            orderId = Long.parseLong(params.get("order_id"));//淘宝订单交易号
            sellerNick = params.get("seller_nick");//淘宝卖家用户名（旺旺号）
        } catch (Exception e) {
            renderJSON("{\"code\":503}");
            return;
        }
        if (!"券生活8".equals(sellerNick)) {
            renderJSON("{\"code\":504}");
            return;//暂时只发我们自己的店
        }
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId",
                OuterOrderPartner.TB, orderId).first();
        if (outerOrder == null) {
            renderJSON("{\"code\":504}");
            return;//没有找到订单
        }
        outerOrder.status = OuterOrderStatus.RESEND_COPY;
        outerOrder.save();
        Logger.info("taobao coupon resend success");
        renderJSON("{\"code\":200}");
    }

    /**
     * 退款逻辑
     *
     * @param params 淘宝传过来的参数
     */
    private static void cancel(Map<String, String> params) {
        Long orderId;
        String sellerNick;
        try {
            orderId = Long.parseLong(params.get("order_id"));//淘宝订单交易号
            sellerNick = params.get("seller_nick");//淘宝卖家用户名（旺旺号）
        } catch (Exception e) {
            renderJSON("{\"code\":503}");
            return;
        }
        if (!"券生活8".equals(sellerNick)) {
            renderJSON("{\"code\":504}");
            return;//暂时只发我们自己的店
        }
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId",
                OuterOrderPartner.TB, orderId).first();
        if (outerOrder == null) {
            renderJSON("{\"code\":504}");
            return;//没找到外部商品ID
        }
        List<ECoupon> eCoupons = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
        for (ECoupon coupon : eCoupons) {
            if (!ECoupon.applyRefund(coupon, coupon.order.userId, coupon.order.userType)
                    .equals(ECoupon.ECOUPON_REFUND_OK)) {
                Logger.error("taobao refund error !!!!!!!! coupon id: %s", coupon.id);
            }
        }
        outerOrder.status = OuterOrderStatus.REFUND_SYNCED;
        outerOrder.save();
        Logger.error("taobao refund success");
        renderJSON("{\"code\":200}");
    }

    /**
     * 修改用户手机号
     *
     * @param params 淘宝传过来的参数
     */
    private static void modified(Map<String, String> params) {
        Long orderId;
        String sellerNick, mobile;
        try {
            orderId = Long.parseLong(params.get("order_id"));//淘宝订单交易号
            sellerNick = params.get("seller_nick");//淘宝卖家用户名（旺旺号）
            mobile = params.get("mobile");//买家新的手机号
        } catch (Exception e) {
            renderJSON("{\"code\":503}");
            return;
        }
        if (!"券生活8".equals(sellerNick)) {
            renderJSON("{\"code\":503}");
            return;//暂时只发我们自己的店
        }
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId",
                OuterOrderPartner.TB, orderId).first();
        if (outerOrder == null) {
            renderJSON("{\"code\":504}");
            return;//没找到外部订单
        }

        List<ECoupon> eCouponList = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
        for (ECoupon coupon : eCouponList) {
            if(coupon.status == ECouponStatus.UNCONSUMED) {
                coupon.orderItems.phone = mobile;
                coupon.orderItems.save();
            }
        }
        outerOrder.status = OuterOrderStatus.RESEND_COPY;
        outerOrder.save();
        Logger.info("taobao modify mobile success");
        renderJSON("{\"code\":200}");//发送的码是跟之前一样的
    }


}
