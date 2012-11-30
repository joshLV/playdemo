package controllers;

import com.google.gson.Gson;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.order.*;
import models.resale.Resaler;
import models.sales.GoodsDeployRelation;
import models.sales.MaterialType;
import models.taobao_coupon.TaobaoCouponUtil;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Controller;

import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author likang
 *         Date: 12-11-27
 */
public class TaobaoCouponAPI extends Controller {
    public static String PHONE_REGEX = "^1[3,5,8]\\d{9}$";

    public static void index(String method) {
        Map<String, String> params = request.params.allSimple();
        Logger.info("taobao coupon request: \n%s", new Gson().toJson(params));
        if (!TaobaoCouponUtil.verifyParam(params)) {
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
                renderJSON("{\"code\":200}");//我们不做任何操作
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
    public static void send(Map<String, String> params) {
        Long orderId, outerIid;
        String mobile, sellerNick, token;
        Integer num;

        try {
            orderId = Long.parseLong(params.get("order_id"));//淘宝订单交易号
            mobile = params.get("mobile");//买家手机号
            num = Integer.parseInt(params.get("num"));//购买的数量
            sellerNick = params.get("seller_nick");//淘宝卖家用户名（旺旺号）
            outerIid = Long.parseLong(params.get("outer_iid"));//商家发布商品时填写的外部商品ID
            token = params.get("token");//token验证串，回调时需要传的
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
        //如果找不到该orderCode的订单，说明还没有新建，则新建一个
        if (outerOrder == null) {
            outerOrder = new OuterOrder();
            outerOrder.partner = OuterOrderPartner.TB;
            outerOrder.status = OuterOrderStatus.ORDER_COPY;
            outerOrder.save();
            try { // 将订单写入数据库
                JPA.em().flush();
            } catch (Exception e) { // 如果写入失败，说明 已经存在一个相同的orderCode 的订单，则放弃
                Logger.info("taobao coupon request failed: concurrency request");
                renderJSON("{\"code\":504}");
                return;//解析错误
            }
        }

        outerOrder.message = new Gson().toJson(params);
        outerOrder.orderId = orderId;
        outerOrder.save();
        //检查订单数量
        if (num <= 0 || !checkPhone(mobile)) {
            Logger.info("taobao coupon request failed: invalid params");
            renderJSON("{\"code\":505}");
            return;//解析错误
        }

        try {
            // 尝试申请一个行锁
            JPA.em().refresh(outerOrder, LockModeType.PESSIMISTIC_WRITE);
        } catch (PersistenceException e) {
            //没拿到锁 放弃
            Logger.info("taobao coupon request failed: concurrency request");
            renderJSON("{\"code\":506}");
            return;//解析错误
        }

        if (outerOrder.status == OuterOrderStatus.ORDER_COPY) {
            Order ybqOrder = createYbqOrder(outerIid, num, mobile);
            if (ybqOrder == null) {
                renderJSON("{\"code\":507}");
                return;//解析错误
            } else {
                outerOrder.status = OuterOrderStatus.ORDER_DONE;
                outerOrder.ybqOrder = ybqOrder;
                outerOrder.save();
            }
        } else if (outerOrder.status != OuterOrderStatus.ORDER_SYNCED && outerOrder.status != OuterOrderStatus.ORDER_DONE) {
            Logger.info("taobao coupon request failed: wrong order status");
            renderJSON("{\"code\":508}");
            return;
        }
        Logger.info("taobao coupon request success");
        renderJSON("{\"code\":200}");
    }

    public static void resend(Map<String, String> params) {
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
        outerOrder.extra = TaobaoCouponUtil.ACTION_SEND;
        outerOrder.save();
        renderJSON("{\"code\":200}");
    }

    /**
     * 退款逻辑
     *
     * @param params 淘宝传过来的参数
     */
    public static void cancel(Map<String, String> params) {
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
            return;//暂时只发我们自己的店
        }
        List<ECoupon> eCoupons = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
        for (ECoupon coupon : eCoupons) {
            if (!ECoupon.applyRefund(coupon, coupon.order.userId, coupon.order.userType)
                    .equals(ECoupon.ECOUPON_REFUND_OK)) {
                Logger.error("taobao refund error !!!!!!!! coupon id: %s", coupon.id);
            }
        }
        renderJSON("{\"code\":200}");
    }

    // 创建一百券订单
    private static Order createYbqOrder(Long outerGroupId, Integer productNum, String userPhone) {
        Resaler resaler = Resaler.findOneByLoginName(Resaler.TAOBAO_LOGIN_NAME);
        if (resaler == null) {
            Logger.error("can not find the resaler by login name: %s", Resaler.TAOBAO_LOGIN_NAME);
            return null;
        }
        Order ybqOrder = Order.createConsumeOrder(resaler.getId(), AccountType.RESALER);
        ybqOrder.save();
        try {
            GoodsDeployRelation goodsDeployRelation = GoodsDeployRelation.find("byLinkId", outerGroupId).first();
            if (goodsDeployRelation == null || goodsDeployRelation.goods == null) {
                Logger.info("can not find goodsDeployRelation: %s", outerGroupId);
                return null;
            }
            models.sales.Goods goods = goodsDeployRelation.goods;
            if (goods == null) {
                Logger.info("goods not found: %s", outerGroupId);
                return null;
            }

            OrderItems uhuilaOrderItem = ybqOrder.addOrderItem(
                    goods, productNum, userPhone, goods.getResalePrice(), goods.getResalePrice());

            uhuilaOrderItem.save();
            if (goods.materialType.equals(MaterialType.REAL)) {
                ybqOrder.deliveryType = DeliveryType.SMS;
            } else if (goods.materialType.equals(MaterialType.ELECTRONIC)) {
                ybqOrder.deliveryType = DeliveryType.LOGISTICS;
            }
        } catch (NotEnoughInventoryException e) {
            Logger.info("enventory not enough");
            return null;
        }

        ybqOrder.createAndUpdateInventory();
        ybqOrder.accountPay = ybqOrder.needPay;
        ybqOrder.discountPay = BigDecimal.ZERO;
        ybqOrder.payMethod = PaymentSource.getBalanceSource().code;
        ybqOrder.payAndSendECoupon();
        ybqOrder.save();

        return ybqOrder;
    }

    private static boolean checkPhone(String phone) {
        if (phone == null) {
            return false;
        }
        Pattern pattern = Pattern.compile(PHONE_REGEX);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }
}
