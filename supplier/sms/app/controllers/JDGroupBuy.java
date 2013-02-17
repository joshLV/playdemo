package controllers;

import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.jingdong.groupbuy.JDGroupBuyUtil;
import models.jingdong.groupbuy.JingdongMessage;
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
import models.sales.MaterialType;
import models.sales.ResalerProduct;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.libs.IO;
import play.libs.XPath;
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
        Logger.info("jingdong sendOrder request:\n%s", restXml);
        JingdongMessage message = JDGroupBuyUtil.parseMessage(restXml);
        //解析请求
        if (!message.isOk()) {
            finish(201, "parse send_order request xml error");
        }
        Integer count = Integer.parseInt(message.selectTextTrim("./Count"));

        //检查购买数量
        if (count <= 0) {
            finish(202, "the buy number must be a positive one");
        }
        BigDecimal teamPrice = new BigDecimal(message.selectTextTrim("./TeamPrice")).divide(new BigDecimal("100"));
        BigDecimal origin = new BigDecimal(message.selectTextTrim("./Origin")).divide(new BigDecimal("100"));

        //检查订单总额是否匹配
        if (teamPrice.multiply(new BigDecimal(count)).compareTo(origin) != 0) {
            finish(203, "the total amount does not match the team price and count");
        }

        String mobile = message.selectTextTrim("./Mobile");
        //检查手机号
        if (!checkPhone(mobile)) {
            finish(204, "invalid mobile: " + mobile);
        }

        String jdOrderId = message.selectTextTrim("./JdOrderId").trim();
        Long venderTeamId = Long.parseLong(message.selectTextTrim("./VenderTeamId").trim());

        //检查并保存此新请求
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId", OuterOrderPartner.JD, jdOrderId).first();
        //如果找不到该orderCode的订单，说明还没有新建，则新建一个
        if (outerOrder == null) {
            outerOrder = new OuterOrder();
            outerOrder.partner = OuterOrderPartner.JD;
            outerOrder.status = OuterOrderStatus.ORDER_COPY;
            outerOrder.orderId = jdOrderId;
            outerOrder.message = restXml;
            outerOrder.save();
            try { // 将订单写入数据库
                JPA.em().flush();
            } catch (Exception e) { // 如果写入失败，说明 已经存在一个相同的orderId 的订单，则放弃
                finish(205, "there is another parallel request");
            }
        }

        //申请行锁后处理订单
        try {
            // 尝试申请一个行锁
            JPA.em().refresh(outerOrder, LockModeType.PESSIMISTIC_WRITE);
        } catch (PersistenceException e) {
            //没拿到锁 放弃
            finish(206, "there is another parallel request");
        }
        //生成一百券订单
        if (outerOrder.status == OuterOrderStatus.ORDER_COPY) {
            Order ybqOrder = outerOrder.ybqOrder;
            if(ybqOrder == null) {
                ybqOrder = createYbqOrder(venderTeamId, teamPrice, count, mobile);
            }
            outerOrder.status = OuterOrderStatus.ORDER_DONE;
            outerOrder.ybqOrder = ybqOrder;
            outerOrder.message = restXml;
            outerOrder.save();
        }
        List<Node> jdCoupons = message.selectNodes("./Coupons/Coupon");
        //保存京东的券号密码
        List<ECoupon> coupons = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
        if (outerOrder.status == OuterOrderStatus.ORDER_DONE || outerOrder.status == OuterOrderStatus.ORDER_SYNCED) {
            if (coupons.size() != jdCoupons.size()) {
                finish(211, "coupon size not matched, ybq size: " + coupons.size() + " jd size:" + jdCoupons.size());
            }
            // 保存京东的券号密码
            for (int i = 0; i < coupons.size(); i++) {
                ECoupon coupon = coupons.get(i);
                Node jdCoupon = jdCoupons.get(i);

                coupon.partner = ECouponPartner.JD;
                coupon.partnerCouponId = XPath.selectText("./CouponId", jdCoupon).trim();
                coupon.partnerCouponPwd = XPath.selectText("./CouponPwd", jdCoupon).trim();
                coupon.save();
            }
            outerOrder.status = OuterOrderStatus.ORDER_SYNCED;
            outerOrder.save();
        }

        if (outerOrder.status == OuterOrderStatus.ORDER_SYNCED){
            String jdTeamId = message.selectTextTrim("./JdTeamId");

            Template template = TemplateLoader.load("jingdong/groupbuy/response/sendOrder.xml");
            Goods goods = ResalerProduct.getGoods(venderTeamId, OuterOrderPartner.JD);
            Map<String, Object> params = new HashMap<>();
            params.put("jdTeamId", jdTeamId);
            params.put("venderTeamId", venderTeamId);
            params.put("ybqOrder", outerOrder.ybqOrder);
            params.put("coupons", coupons);
            params.put("goods", goods);
            renderArgs.put("data", template.render(params));
            Logger.info("jd send order success: %s", outerOrder.ybqOrder.getId());
            finish(200, "success");
        } else {
            finish(212, "the order has been processed");
        }
    }

    /**
     * 查询团购销量
     */
    public static void queryTeamSellCount() {
        String restXml = IO.readContentAsString(request.body);
        Logger.info("jingdong queryTeamSellCount request:\n%s", restXml);
        JingdongMessage message = JDGroupBuyUtil.parseMessage(restXml);

        //解析请求
        if (!message.isOk()) {
            finish(201, "parse query_team_sell_count request xml error");
        }
        Long venderTeamId = Long.parseLong(message.selectTextTrim("./VenderTeamId"));

        //查询商品
        Goods goods = ResalerProduct.getGoods(venderTeamId, OuterOrderPartner.JD);
        if (goods == null) {
            finish(202, "goods not found");
        }

        //响应
        Template template = TemplateLoader.load("jingdong/groupbuy/response/queryTeamSellCount.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("venderTeamId", venderTeamId);
        params.put("sellCount", goods.getVirtualSaleCount());
        renderArgs.put("data", template.render(params));
        finish(200, "success");
    }

    /**
     * 处理退款请求
     */
    public static void sendOrderRefund() {
        String restXml = IO.readContentAsString(request.body);
        Logger.info("jingdong sendOrderRefund request:\n%s", restXml);
        JingdongMessage message = JDGroupBuyUtil.parseMessage(restXml);

        if (!message.isOk()) {
            finish(201, "parse send_order_refund request xml error");
        }

        //以京东分销商的身份申请退款
        Resaler resaler = Resaler.findOneByLoginName(Resaler.JD_LOGIN_NAME);
        if (resaler == null) {
            finish(202, "can not find the jingdong resaler");
        }

        String venderOrderId = message.selectTextTrim("./VenderOrderId");
        Long jdOrderId = Long.parseLong(message.selectTextTrim("./JdOrderId"));

        Order order = Order.find("byOrderNumber", venderOrderId).first();
        if (order == null) {
            finish(203, "can not find ybq_order: " + venderOrderId);
        }

        List<Node> jdCoupons = message.selectNodes("./Coupons/Coupon");

        //处理退款
        List<String> refundedCoupons = new ArrayList<>();
        for (Node coupon : jdCoupons) {
            ECoupon eCoupon = ECoupon.find("byOrderAndPartnerAndPartnerCouponId",
                    order, ECouponPartner.JD, coupon.getTextContent()).first();
            if (eCoupon != null) {
                String ret = ECoupon.applyRefund(eCoupon, resaler.getId(), AccountType.RESALER);
                if (ret.equals(ECoupon.ECOUPON_REFUND_OK)) {
                    Logger.info("jingdong refund ok, ybq couponId: %s", eCoupon.getId());
                    refundedCoupons.add(eCoupon.partnerCouponId);
                }
            }
        }

        //响应
        Template template = TemplateLoader.load("jingdong/groupbuy/response/sendOrderRefund.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("jdOrderId", jdOrderId);
        params.put("venderOrderId", venderOrderId);
        params.put("coupons", refundedCoupons);
        renderArgs.put("data", template.render(params));
        finish(200, "success");
    }

    public static void sendSms() {
        String restXml = IO.readContentAsString(request.body);
        Logger.info("jingdong sendSms request:\n%s", restXml);
        JingdongMessage message = JDGroupBuyUtil.parseMessage(restXml);
        if (!message.isOk()) {
            finish(201, "parse send_sms_request request xml error");
        }
        String mobile = message.selectTextTrim("./Mobile");
        String venderCouponId = message.selectTextTrim("./VenderCouponId");
        String jdCouponId = message.selectTextTrim("./JdCouponId");

        if (!checkPhone(mobile)) {
            finish(202, "invalid mobile");
        }

        //重发短信
        ECoupon coupon = ECoupon.find("byECouponSnAndPartnerAndPartnerCouponId",
                venderCouponId, ECouponPartner.JD, jdCouponId).first();
        if (coupon == null) {
            finish(300, "coupon not found");
        }
        if (coupon.status == ECouponStatus.REFUND) {
            finish(301, "coupon refunded");
        }
        if (coupon.smsSentCount >= 3) {
            finish(302, "reach the limit of download time");
        }

        //coupon.downloadTimes = coupon.downloadTimes - 1;

        // 如果是刚创建的券，不再发短信，直接返回，否则发一次短信
        Long currentTime = (new Date()).getTime();
        if (currentTime - coupon.createdAt.getTime() > 600000l) { //10分钟之后的请求才会重新发短信
            coupon.sendOrderSMS(mobile, "京东短信重发");
        } else {
            Logger.info("Coupon(%s)生成于%s, 10分钟内不会重发短信.", coupon.id, coupon.createdAt);
        }

        //响应
        Template template = TemplateLoader.load("jingdong/groupbuy/response/sendSms.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("jdCouponId", jdCouponId);
        params.put("venderCouponId", venderCouponId);
        params.put("mobile", mobile);
        renderArgs.put("data", template.render(params));
        finish(200, "success");
    }


    // 创建一百券订单
    private static Order createYbqOrder(Long venderTeamId, BigDecimal teamPrice, int count, String mobile) {
        Resaler resaler = Resaler.findOneByLoginName(Resaler.JD_LOGIN_NAME);
        Logger.info("create ybq order");
        if (resaler == null) {
            finish(207, "can not find the jingdong resaler");
        }
        Order ybqOrder = Order.createConsumeOrder(resaler.getId(), AccountType.RESALER);
        ybqOrder.save();
        try {
            Goods goods = ResalerProduct.getGoods(venderTeamId, OuterOrderPartner.JD);
            if (goods == null) {
                finish(208, "can not find goods: " + venderTeamId);
            }
            if (goods.originalPrice.compareTo(teamPrice) > 0) {
                finish(209, "invalid product price: " + teamPrice);
            }

            OrderItems uhuilaOrderItem = ybqOrder.addOrderItem( goods, count, mobile, teamPrice, teamPrice);
            uhuilaOrderItem.save();
            if (goods.materialType.equals(MaterialType.REAL)) {
                ybqOrder.deliveryType = DeliveryType.LOGISTICS;
            } else if (goods.materialType.equals(MaterialType.ELECTRONIC)) {
                ybqOrder.deliveryType = DeliveryType.SMS;
            }
        } catch (NotEnoughInventoryException e) {
            JPA.em().getTransaction().rollback();
            finish(210, "inventory not enough");
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
        Logger.info("jingdong groupbuy api: %s %s", resultCode, resultMessage);
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
