package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import models.resale.Resaler;
import models.taobao.TaobaoCouponUtil;
import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
            Logger.warn("taobao coupon request error: param verify failed!");
            renderJSON("{\"code\":501}");
            return;//签名错误
        }
        String orderId;
        String sellerNick;
        try {
            orderId = params.get("order_id").trim();//淘宝订单交易号
            sellerNick = params.get("seller_nick").trim();//淘宝卖家用户名（旺旺号）
        } catch (Exception e) {
            Logger.warn("taobao coupon request error: param invalid");
            renderJSON("{\"code\":502}");
            return;
        }
        //todo 判断其他的淘宝账户
        if (!"order_modify".equals(method) && !"券生活8".equals(sellerNick) && !"kisbear".equals(sellerNick)) {
            Logger.warn("taobao coupon request error: wrong seller nick: %s", sellerNick);
            renderJSON("{\"code\":503}");
            return;//暂时只发我们自己的店
        }

        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId", OuterOrderPartner.TB, orderId).first();

        switch (method) {
            case "send":
                send(sellerNick, params, orderId, outerOrder);
                break;
            case "resend":
                resend(outerOrder);
                break;
            case "cancel":
                cancel(outerOrder);
                break;
            case "modified"://用户修改手机号
                mobileModified(params, outerOrder);
                break;
            case "order_modify"://订单修改通知
                orderModify(params, outerOrder);
                break;
            default:
                throw new IllegalArgumentException("no such method");
        }
    }

    /**
     * 接收发码通知
     * 此处只接收、记录请求内容，并立即返回，具体工作由 TaobaoCouponConsumer 来做
     */
    private static void send(String sellerNick, Map<String, String> params, String orderId, OuterOrder outerOrder) {
        Resaler resaler = Resaler.findApprovedByLoginName(Resaler.TAOBAO_LOGIN_NAME);
        if (resaler == null) {
            Logger.error("can not find the resaler by login name: %s", Resaler.TAOBAO_LOGIN_NAME);
            return;
        }
        //如果是从其他淘宝店铺过来的订单，则读取相应的分销信息
        if ("kisbear".equals(sellerNick)) {
            resaler = Resaler.findApprovedByLoginName(Resaler.YLD_LOGIN_NAME);
        }
        if (resaler == null) {
            Logger.error("can not find the resaler by login name: %s", Resaler.YLD_LOGIN_NAME);
            return;
        }
        //如果找不到该orderCode的订单，说明还没有新建，则新建一个
        if (outerOrder == null) {
            outerOrder = new OuterOrder();
            outerOrder.resaler = resaler;
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

    /**
     * 处理重发请求
     * 此处只接收、记录请求内容，并立即返回，具体工作由 TaobaoCouponConsumer 来做
     */
    private static void resend(OuterOrder outerOrder) {
        if (outerOrder == null) {
            Logger.warn("taobao coupon resend error: outerOrder not found");
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
     */
    private static void cancel(OuterOrder outerOrder) {
        if (outerOrder == null) {
            Logger.warn("taobao coupon cancel error: outerOrder not found");
            renderJSON("{\"code\":504}");
            return;//没找到外部商品ID
        }
        List<ECoupon> eCoupons = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
        for (ECoupon coupon : eCoupons) {
            final String errInfo = ECoupon.applyRefund(coupon);
            if (!errInfo.equals(ECoupon.ECOUPON_REFUND_OK)) {
                Logger.error("taobao refund error !!!!!!!! coupon id: %s. %s", coupon.id, errInfo);
            }
        }
        outerOrder.status = OuterOrderStatus.REFUND_SYNCED;
        outerOrder.save();
        Logger.info("taobao refund success");
        renderJSON("{\"code\":200}");
    }

    /**
     * 修改用户手机号
     *
     * @param params 淘宝传过来的参数
     */
    private static void mobileModified(Map<String, String> params, OuterOrder outerOrder) {
        String mobile = params.get("mobile");//买家新的手机号
        if (outerOrder == null) {
            Logger.warn("taobao coupon mobileModify error: outerOrder not found");
            renderJSON("{\"code\":504}");
            return;//没找到外部订单
        }

        List<ECoupon> eCouponList = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
        for (ECoupon coupon : eCouponList) {
            if (coupon.status == ECouponStatus.UNCONSUMED) {
                coupon.orderItems.phone = mobile;
                coupon.orderItems.save();
            }
        }
        outerOrder.status = OuterOrderStatus.RESEND_COPY;
        outerOrder.save();
        Logger.info("taobao modify mobile success");
        renderJSON("{\"code\":200}");//发送的码是跟之前一样的
    }

    /**
     * 淘宝订单修改了，目前只有有效期更改的通知
     */
    private static void orderModify(Map<String, String> params, OuterOrder outerOrder) {
        if (outerOrder == null) {
            Logger.warn("taobao coupon orderModify error: outerOrder not found");
            renderJSON("{\"code\":504}");
            return;//没找到外部订单
        }

        String subMethod = params.get("sub_method");
        String data = params.get("data");
        JsonObject dataJson;
        try {
            dataJson = new JsonParser().parse(data).getAsJsonObject();
        } catch (Exception e) {
            Logger.warn("taobao coupon order modify failed: can not parse data as json %s", data);
            renderJSON("{\"code\":505}");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        switch (subMethod) {
            case "1":
                List<ECoupon> couponList = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
                //修改生效时间
                if (dataJson.has("valid_start")) {
                    Date validStart = null;
                    try {
                        validStart = dateFormat.parse(dataJson.get("valid_start").getAsString());
                    } catch (ParseException e) {
                        Logger.warn("taobao coupon order modify failed: parse date error", data);
                        renderJSON("{\"code\":507}");
                        return;
                    }
                    for (ECoupon coupon : couponList) {
                        if (coupon.status == ECouponStatus.UNCONSUMED) {
                            coupon.effectiveAt = validStart;
                            coupon.save();
                        }
                    }
                }
                //修改过期时间
                if (dataJson.has("valid_ends")) {
                    Date validEnd = null;
                    try {
                        validEnd = dateFormat.parse(dataJson.get("valid_ends").getAsString());
                    } catch (ParseException e) {
                        Logger.warn("taobao coupon order modify failed: parse date error", data);
                        renderJSON("{\"code\":508}");
                        return;
                    }
                    for (ECoupon coupon : couponList) {
                        if (coupon.status == ECouponStatus.UNCONSUMED) {
                            coupon.expireAt = validEnd;
                            coupon.save();
                        }
                    }
                }
                renderJSON("{\"code\":200}");
                break;
            default:
                Logger.warn("taobao coupon order modify failed: unknown sub method: %s", subMethod);
                renderJSON("{\"code\":506}");
                break;
        }
    }
}
