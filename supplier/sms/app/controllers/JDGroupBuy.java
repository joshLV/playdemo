package controllers;

import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.jingdong.groupbuy.JDGroupBuyUtil;
import models.jingdong.groupbuy.JDRest;
import models.jingdong.groupbuy.request.CouponRequest;
import models.jingdong.groupbuy.request.QueryTeamSellCountRequest;
import models.jingdong.groupbuy.request.SendOrderRefundRequest;
import models.jingdong.groupbuy.request.SendOrderRequest;
import models.jingdong.groupbuy.request.SendSmsRequest;
import models.order.DeliveryType;
import models.order.ECoupon;
import models.order.ECouponPartner;
import models.order.ECouponStatus;
import models.order.NotEnoughInventoryException;
import models.order.Order;
import models.order.OrderItems;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.GoodsDeployRelation;
import models.sales.MaterialType;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.libs.IO;
import play.mvc.Before;
import play.mvc.Controller;
import play.templates.Template;
import play.templates.TemplateLoader;

import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author likang
 *         Date: 12-9-28
 */
public class JDGroupBuy extends Controller {
    public static String DATE_FORMAT = "yyy-MM-dd HH:mm:ss";

    public static String PHONE_REGEX = "^1\\d{10}$";

    private static String canMock = Play.configuration.getProperty("mock.api.ui", "disable");

    /**
     * 基本相应参数
     */
    @Before
    public static void baseResponse() {
        if (!"enabled".equals(canMock)) {
            renderArgs.put("encrypt", "true");
        } else {
            renderArgs.put("encrypt", "false");
        }
        renderArgs.put("version", "1.0");
        renderArgs.put("venderId", JDGroupBuyUtil.VENDER_ID);
        renderArgs.put("zip", "false");
    }

    /**
     * 订单
     */
    public static void sendOrder() {
        String restXml = IO.readContentAsString(request.body);
        //解析请求
        JDRest<SendOrderRequest> sendOrderJDRest = new JDRest<>();
        if (!sendOrderJDRest.parse(restXml, new SendOrderRequest())) {
            //解析失败
            Logger.info("parse send_order request xml error");
            finish(201, "parse send_order request xml error");
            return;
        }
        SendOrderRequest sendOrderRequest = sendOrderJDRest.data;

        //检查购买数量
        if (sendOrderRequest.count <= 0) {
            Logger.info("the buy number must be a positive one");
            finish(202, "the buy number must be a positive one");
            return;
        }

        //检查订单总额是否匹配
        if (sendOrderRequest.teamPrice
                .multiply(new BigDecimal(sendOrderRequest.count))
                .compareTo(sendOrderRequest.origin) != 0) {
            Logger.info("the total amount does not match the team price and count");
            finish(203, "the total amount does not match the team price and count");
            return;
        }

        //检查手机号
        if (!checkPhone(sendOrderRequest.mobile)) {
            Logger.info("invalid mobile");
            finish(204, "invalid mobile");
        }

        //检查并保存此新请求
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId",
                OuterOrderPartner.JD, sendOrderRequest.jdOrderId).first();
        //如果找不到该orderCode的订单，说明还没有新建，则新建一个
        if (outerOrder == null) {
            outerOrder = new OuterOrder();
            outerOrder.partner = OuterOrderPartner.JD;
            outerOrder.status = OuterOrderStatus.ORDER_COPY;
            outerOrder.orderId = sendOrderRequest.jdOrderId;
            outerOrder.message = restXml;
            outerOrder.save();
            try { // 将订单写入数据库
                JPA.em().flush();
            } catch (Exception e) { // 如果写入失败，说明 已经存在一个相同的orderId 的订单，则放弃
                Logger.info("flush failed");
                finish(205, "there is another parallel request");
                return;
            }
        }

        //申请行锁后处理订单
        try {
            // 尝试申请一个行锁
            JPA.em().refresh(outerOrder, LockModeType.PESSIMISTIC_WRITE);
        } catch (PersistenceException e) {
            //没拿到锁 放弃
            Logger.info("failed to request persistence lock");
            finish(206, "there is another parallel request");
            return;
        }
        //生成一百券订单
        if (outerOrder.status == OuterOrderStatus.ORDER_COPY) {
            Order ybqOrder = outerOrder.ybqOrder;
            if(ybqOrder == null) {
                ybqOrder = createYbqOrder(sendOrderRequest);
            }
            outerOrder.status = OuterOrderStatus.ORDER_DONE;
            outerOrder.ybqOrder = ybqOrder;
            outerOrder.message = restXml;
            outerOrder.save();
        }
        //保存京东的券号密码
        List<ECoupon> coupons = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
        if (outerOrder.status == OuterOrderStatus.ORDER_DONE) {
            if (coupons.size() != sendOrderRequest.coupons.size()) {
                Logger.info("coupon size not matched, ybq size: %s, jd size: %s", coupons.size(), sendOrderRequest.coupons.size());
                finish(211, "coupon size not matched, ybq size: " + coupons.size() + " jd size:" + sendOrderRequest.coupons.size());
            }
            // 保存京东的券号密码
            for (int i = 0; i < coupons.size(); i++) {
                ECoupon coupon = coupons.get(i);
                CouponRequest jdCoupon = sendOrderRequest.coupons.get(i);
                coupon.partner = ECouponPartner.JD;
                coupon.partnerCouponId = jdCoupon.couponId;
                coupon.partnerCouponPwd = jdCoupon.couponPwd;
                coupon.save();
            }
            outerOrder.status = OuterOrderStatus.ORDER_SYNCED;
            outerOrder.save();
        }

        if (outerOrder.status == OuterOrderStatus.ORDER_SYNCED){
            Template template = TemplateLoader.load("jingdong/groupbuy/response/sendOrder.xml");
            Goods goods = GoodsDeployRelation.getGoods(OuterOrderPartner.JD, sendOrderRequest.venderTeamId);
            Map<String, Object> params = new HashMap<>();
            params.put("sendOrderRequest", sendOrderRequest);
            params.put("ybqOrder", outerOrder.ybqOrder);
            params.put("coupons", coupons);
            params.put("goods", goods);
            renderArgs.put("data", template.render(params));
            Logger.info("jd send order success: %s", outerOrder.ybqOrder.getId());
            finish(200, "success");
        } else {
            Logger.info("order status is not ORDER_DONE, instead it's %s", outerOrder.status);
            finish(212, "the order has been processed");
        }
    }

    /**
     * 查询团购销量
     */
    public static void queryTeamSellCount() {
        String restXml = IO.readContentAsString(request.body);

        //解析请求
        JDRest<QueryTeamSellCountRequest> sendOrderJDRest = new JDRest<>();
        if (!sendOrderJDRest.parse(restXml, new QueryTeamSellCountRequest())) {
            Logger.info("parse query_team_sell_count xml error");
            finish(201, "parse query_team_sell_count request xml error");
            return;
        }
        QueryTeamSellCountRequest queryTeamSellCountRequest = sendOrderJDRest.data;

        //查询商品
        Goods goods = GoodsDeployRelation.getGoods(OuterOrderPartner.JD, queryTeamSellCountRequest.venderTeamId);
        if (goods == null) {
            Logger.info("goods not found");
            finish(202, "goods not found");
            return;
        }

        //响应
        Template template = TemplateLoader.load("jingdong/groupbuy/response/queryTeamSellCount.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("goods", goods);
        renderArgs.put("data", template.render(params));
        Logger.info("queryTeamSellCount success");
        finish(200, "success");
    }

    /**
     * 处理退款请求
     */
    public static void sendOrderRefund() {
        Logger.info("start jingodng send order refund");
        String restXml = IO.readContentAsString(request.body);
        //解析请求
        JDRest<SendOrderRefundRequest> sendOrderJDRest = new JDRest<>();
        if (!sendOrderJDRest.parse(restXml, new SendOrderRefundRequest())) {
            Logger.info("parse sendOrderRefund xml error");
            finish(201, "parse send_order_refund request xml error");
            return;
        }
        SendOrderRefundRequest sendOrderRefundRequest = sendOrderJDRest.data;

        //以京东分销商的身份申请退款
        Resaler resaler = Resaler.findOneByLoginName(Resaler.JD_LOGIN_NAME);
        if (resaler == null) {
            Logger.error("can not find the resaler by login name: %s", Resaler.JD_LOGIN_NAME);
            finish(202, "can not find the jingdong resaler");
            return;
        }

        Order order = Order.find("byOrderNumber", sendOrderRefundRequest.venderOrderId).first();
        if (order == null) {
            Logger.error("can not find the ybq_order: %s", sendOrderRefundRequest.venderOrderId);
            finish(203, "can not find ybq_order: " + sendOrderRefundRequest.venderOrderId);
            return;
        }

        //处理退款
        List<CouponRequest> refundedCoupons = new ArrayList<>();
        for (CouponRequest coupon : sendOrderRefundRequest.coupons) {
            ECoupon eCoupon = ECoupon.find("byOrderAndPartnerAndPartnerCouponId",
                    order, ECouponPartner.JD, coupon.couponId).first();
            if (eCoupon != null) {
                String ret = ECoupon.applyRefund(eCoupon, resaler.getId(), AccountType.RESALER);
                if (ret.equals(ECoupon.ECOUPON_REFUND_OK)) {
                    Logger.info("jingdong refund ok, ybq couponId: %s", eCoupon.getId());
                    refundedCoupons.add(coupon);
                }
            }
        }

        //响应
        Template template = TemplateLoader.load("jingdong/groupbuy/response/sendOrderRefund.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("sendOrderRefundRequest", sendOrderRefundRequest);
        params.put("coupons", refundedCoupons);
        renderArgs.put("data", template.render(params));
        Logger.info("send order refund success");
        finish(200, "success");
    }

    public static void sendSms() {
        Logger.info("start send sms on jingdong groupbuy");
        String restXml = IO.readContentAsString(request.body);
        //解析请求
        JDRest<SendSmsRequest> sendSmsRequestJDRest = new JDRest<>();
        if (!sendSmsRequestJDRest.parse(restXml, new SendSmsRequest())) {
            Logger.info("parse send_sms_request xml error");
            finish(201, "parse send_sms_request request xml error");
            return;
        }
        SendSmsRequest sendSmsRequest = sendSmsRequestJDRest.data;

        if (!checkPhone(sendSmsRequest.mobile)) {
            Logger.info("invalid mobile");
            finish(202, "invalid mobile");
        }

        //重发短信
        ECoupon coupon = ECoupon.find("byECouponSnAndPartnerAndPartnerCouponId",
                sendSmsRequest.venderCouponId, ECouponPartner.JD, sendSmsRequest.jdCouponId).first();
        if (coupon == null) {
            Logger.info("coupon not found");
            finish(300, "coupon not found");
            return;
        }
        if (coupon.status == ECouponStatus.REFUND) {
            Logger.info("coupon refunded");
            finish(301, "coupon refunded");
        }
        if (coupon.downloadTimes <= 0) {
            Logger.info("reach the limit of download time");
            finish(302, "reach the limit of download time");
        }

        //coupon.downloadTimes = coupon.downloadTimes - 1;

        // 如果是刚创建的券，不再发短信，直接返回，否则发一次短信
        Long currentTime = (new Date()).getTime();
        if (currentTime - coupon.createdAt.getTime() > 600000l) { //10分钟之后的请求才会重新发短信
            coupon.sendOrderSMS(sendSmsRequest.mobile, "京东短信重发");
        } else {
            Logger.info("Coupon(id:" + coupon.id + ")生成于" + coupon.createdAt + ", 10分钟内不会重发短信.");
        }

        //响应
        Template template = TemplateLoader.load("jingdong/groupbuy/response/sendSms.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("sendSmsRequest", sendSmsRequest);
        renderArgs.put("data", template.render(params));
        Logger.info("jingdong sms send success");
        finish(200, "success");
    }


    // 创建一百券订单
    private static Order createYbqOrder(SendOrderRequest sendOrderRequest) {
        Resaler resaler = Resaler.findOneByLoginName(Resaler.JD_LOGIN_NAME);
        Logger.info("create ybq order");
        if (resaler == null) {
            Logger.error("can not find the resaler by login name: %s", Resaler.JD_LOGIN_NAME);
            finish(207, "can not find the jingdong resaler");
            return null;
        }
        Order ybqOrder = Order.createConsumeOrder(resaler.getId(), AccountType.RESALER);
        ybqOrder.save();
        try {
            Goods goods = GoodsDeployRelation.getGoods(OuterOrderPartner.JD, sendOrderRequest.venderTeamId);
            if (goods == null) {
                Logger.info("goods not found: %s", sendOrderRequest.venderTeamId);
                finish(208, "can not find goods: " + sendOrderRequest.venderTeamId);
                return null;
            }
            if (goods.originalPrice.compareTo(sendOrderRequest.teamPrice) > 0) {
                Logger.info("invalid yhd productPrice: %s", sendOrderRequest.teamPrice);
                finish(209, "invalid product price: " + sendOrderRequest.teamPrice);
                return null;
            }

            OrderItems uhuilaOrderItem = ybqOrder.addOrderItem(
                    goods,
                    sendOrderRequest.count,
                    sendOrderRequest.mobile,
                    sendOrderRequest.teamPrice,
                    sendOrderRequest.teamPrice);
            uhuilaOrderItem.save();
            if (goods.materialType.equals(MaterialType.REAL)) {
                ybqOrder.deliveryType = DeliveryType.LOGISTICS;
            } else if (goods.materialType.equals(MaterialType.ELECTRONIC)) {
                ybqOrder.deliveryType = DeliveryType.SMS;
            }
        } catch (NotEnoughInventoryException e) {
            Logger.info("inventory not enough");
            JPA.em().getTransaction().rollback();
            finish(210, "inventory not enough");
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

    private static void finish(int resultCode, String resultMessage) {
        renderArgs.put("resultCode", resultCode);
        renderArgs.put("resultMessage", resultMessage);
        renderTemplate("jingdong/groupbuy/response/main.xml");
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
