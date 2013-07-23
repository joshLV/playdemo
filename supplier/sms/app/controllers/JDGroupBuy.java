package controllers;

import facade.order.OuterOrderFacade;
import facade.order.translate.JDOrderMessage;
import facade.order.translate.JDOrderMessageTranslate;
import facade.order.vo.OuterECouponVO;
import facade.order.vo.OuterOrderItemVO;
import facade.order.vo.OuterOrderResult;
import facade.order.vo.OuterOrderVO;
import models.jingdong.groupbuy.JDGroupBuyUtil;
import models.jingdong.groupbuy.JingdongMessage;
import models.order.ECoupon;
import models.order.ECouponPartner;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.ResalerProduct;
import org.w3c.dom.Node;
import play.Logger;
import play.Play;
import play.libs.IO;
import play.libs.XPath;
import play.mvc.Before;
import play.mvc.Controller;
import play.templates.Template;
import play.templates.TemplateLoader;

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
     * 生成订单
     */
    public static void sendOrder() {
        final String restXml = IO.readContentAsString(request.body);
        Logger.info("jingdong sendOrder request:\n%s", restXml);
        final JingdongMessage message = JDGroupBuyUtil.parseMessage(restXml);
        //解析请求
        if (!message.isOk()) {
            finish(201, "parse send_order request xml error");
        }

        Resaler resaler = Resaler.findApprovedByLoginName(Resaler.JD_LOGIN_NAME);
        Integer count = Integer.parseInt(message.selectTextTrim("./Count"));
        BigDecimal teamPrice = new BigDecimal(message.selectTextTrim("./TeamPrice")).divide(new BigDecimal("100"));
        BigDecimal origin = new BigDecimal(message.selectTextTrim("./Origin")).divide(new BigDecimal("100"));
        String mobile = message.selectTextTrim("./Mobile");
        String jdOrderId = message.selectTextTrim("./JdOrderId").trim();
        Long venderTeamId = Long.parseLong(message.selectTextTrim("./VenderTeamId").trim());

        OuterOrderVO outerOrderVO = OuterOrderVO.build(resaler)
                .outerOrderId(jdOrderId)
                .totalAmount(origin)   //总价
                .mobile(mobile)
                .message(restXml);

        OuterOrderItemVO outerOrderItemVO = OuterOrderItemVO.build()
                .venderTeamId(venderTeamId)
                .count(count).price(teamPrice);

        outerOrderVO.addItem(outerOrderItemVO);

        List<Node> jdCoupons = message.selectNodes("./Coupons/Coupon");
        // 保存京东的券号密码
        for (int i = 0; i < jdCoupons.size(); i++) {
            Node jdCoupon = jdCoupons.get(i);
            OuterECouponVO outerECouponVO = new OuterECouponVO();
            outerECouponVO.eCouponSN = XPath.selectText("./CouponId", jdCoupon).trim();
            outerECouponVO.eCouponPassword = XPath.selectText("./CouponPwd", jdCoupon).trim();
            outerOrderItemVO.addECouponVO(outerECouponVO);
        }

        OuterOrderResult outerOrderResult = OuterOrderFacade.createOuterOrder(outerOrderVO);

        //记录并翻译成京东的消息.
        recordResultMessage(outerOrderResult.getOuterOrderMessage(new JDOrderMessageTranslate()));

        if (outerOrderResult.outerOrder.status == OuterOrderStatus.ORDER_SYNCED) {
            String jdTeamId = message.selectTextTrim("./JdTeamId");
            Template template = TemplateLoader.load("jingdong/groupbuy/response/sendOrder.xml");

            Goods goods = ResalerProduct.getGoods(resaler, venderTeamId, OuterOrderPartner.JD);
            Map<String, Object> params = new HashMap<>();
            params.put("jdTeamId", jdTeamId);
            params.put("venderTeamId", venderTeamId);
            params.put("ybqOrder", outerOrderResult.outerOrder.ybqOrder);
            params.put("coupons", outerOrderResult.eCoupons);
            params.put("goods", goods);
            renderArgs.put("data", template.render(params));
            Logger.info("jd send order success: %s", outerOrderResult.outerOrder.ybqOrder.getId());
            recordResultMessage(200, "success");  // done.
        } else {
            recordResultMessage(212, "the order has been processed");
        }

        renderTemplate("jingdong/groupbuy/response/main.xml");
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
        Resaler resaler = Resaler.findApprovedByLoginName(Resaler.JD_LOGIN_NAME);
        Goods goods = ResalerProduct.getGoods(resaler, venderTeamId, OuterOrderPartner.JD);
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
        String venderOrderId = message.selectTextTrim("./VenderOrderId");
        Long jdOrderId = Long.parseLong(message.selectTextTrim("./JdOrderId"));

        //以京东分销商的身份申请退款
        Resaler resaler = Resaler.findOneByLoginName(Resaler.JD_LOGIN_NAME);
        if (resaler == null) {
            finish(202, "can not find the jingdong resaler");
        }

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
                String ret = ECoupon.applyRefund(eCoupon);
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

    /**
     * 重发短信.
     */
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

    private static void finish(int resultCode, String resultMessage) {
        Logger.info("jingdong groupbuy api: %s %s", resultCode, resultMessage);
        renderArgs.put("resultCode", resultCode);
        renderArgs.put("resultMessage", resultMessage);
        renderTemplate("jingdong/groupbuy/response/main.xml");
    }

    private static void recordResultMessage(int resultCode, String resultMessage) {
        renderArgs.put("resultCode", resultCode);
        renderArgs.put("resultMessage", resultMessage);
    }

    private static void recordResultMessage(JDOrderMessage jdOrderMessage) {
        renderArgs.put("resultCode", jdOrderMessage.code);
        renderArgs.put("resultMessage", jdOrderMessage.message);
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
