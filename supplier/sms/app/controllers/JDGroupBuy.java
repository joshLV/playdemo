package controllers;

import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.jingdong.JDGroupBuyUtil;
import models.jingdong.groupbuy.JDResponse;
import models.jingdong.groupbuy.request.CouponRequest;
import models.jingdong.groupbuy.request.QueryTeamSellCountRequest;
import models.jingdong.groupbuy.request.SendOrderRefundRequest;
import models.jingdong.groupbuy.request.SendOrderRequest;
import models.order.*;
import models.resale.Resaler;
import models.sales.MaterialType;
import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Before;
import play.mvc.Controller;
import play.templates.Template;
import play.templates.TemplateLoader;

import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 * Date: 12-9-28
 */
public class JDGroupBuy extends Controller{
    public static String DATE_FORMAT = "yyy-MM-dd HH:mm:ss";

    public static String PHONE_REGEX = "^1[3,5,8]\\d{9}$";

    /**
     * 基本相应参数
     */
    @Before
    public static void baseResponse(){
        renderArgs.put("version", "1.0");
        renderArgs.put("venderId", JDGroupBuyUtil.VENDER_ID);
        renderArgs.put("encrypt", "true");
        renderArgs.put("zip","false");
    }

    /**
     * 订单
     */
    public static void sendOrder(){
        String restXml = request.body.toString();

        //解析请求
        JDResponse<SendOrderRequest> sendOrderJDResponse = new JDResponse<>();
        if(!sendOrderJDResponse.parse(restXml, new SendOrderRequest())){
            //解析失败
            finish(201, "parse send_order request xml error"); return;
        }
        SendOrderRequest sendOrderRequest = sendOrderJDResponse.data;

        //检查并保存此新请求
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId",
                OuterOrderPartner.JD, sendOrderRequest.jdOrderId).first();
        //如果找不到该orderCode的订单，说明还没有新建，则新建一个
        if(outerOrder == null){
            outerOrder = new OuterOrder();
            outerOrder.partner = OuterOrderPartner.JD;
            outerOrder.status = OuterOrderStatus.ORDER_COPY;
            outerOrder.orderId = sendOrderRequest.jdOrderId;
            outerOrder.message = restXml;
            outerOrder.save();
            try{ // 将订单写入数据库
                JPA.em().flush();
            }catch (Exception e){ // 如果写入失败，说明 已经存在一个相同的orderId 的订单，则放弃
                finish(202, "there is another parallel request");return;
            }
        }else {
            outerOrder.message = restXml;
            outerOrder.save();
        }

        //申请行锁后处理订单
        try{
            // 尝试申请一个行锁
            JPA.em().refresh(outerOrder, LockModeType.PESSIMISTIC_WRITE);
        }catch (PersistenceException e){
            //没拿到锁 放弃
            finish(202, "there is another parallel request"); return;
        }
        if (outerOrder.status == OuterOrderStatus.ORDER_COPY){
            Order ybqOrder = createYbqOrder(sendOrderRequest);
            outerOrder.status = OuterOrderStatus.ORDER_DONE;
            outerOrder.ybqOrder = ybqOrder;
            outerOrder.save();
        }

        if(outerOrder.status != OuterOrderStatus.ORDER_CANCELED){
            Template template = TemplateLoader.load("jingdong/groupbuy/response/sendOrder.xml");
            Map<String, Object> params = new HashMap<>();
            params.put("sendOrderRequest", sendOrderRequest);
            params.put("ybqOrder", outerOrder.ybqOrder);
            renderArgs.put("data", template.render(params));
            finish(200, "success");
        }else {
            finish(207, "order canceled");
        }
    }

    /**
     * 查询团购销量
     */
    public static void queryTeamSellCount(){
        String restXml = request.body.toString();
        //解析请求
        JDResponse<QueryTeamSellCountRequest> sendOrderJDResponse = new JDResponse<>();
        if(!sendOrderJDResponse.parse(restXml, new QueryTeamSellCountRequest())){
            finish(201, "parse query_team_sell_count request xml error"); return;
        }
        QueryTeamSellCountRequest queryTeamSellCountRequest = sendOrderJDResponse.data;

        //查询商品
        models.sales.Goods goods = models.sales.Goods.findById(queryTeamSellCountRequest.venderTeamId);
        if(goods == null){
            finish(202, "goods not found"); return;
        }

        //响应
        Template template = TemplateLoader.load("jingdong/groupbuy/response/queryTeamSellCount.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("goods", goods);
        renderArgs.put("data", template.render(params));
        finish(200, "success");
    }

    /**
     * 处理退款请求
     */
    public static void sendOrderRefund(){
        String restXml = request.body.toString();
        //解析请求
        JDResponse<SendOrderRefundRequest> sendOrderJDResponse = new JDResponse<>();
        if(!sendOrderJDResponse.parse(restXml, new SendOrderRefundRequest())){
            finish(201, "parse query_team_sell_count request xml error"); return;
        }
        SendOrderRefundRequest sendOrderRefundRequest = sendOrderJDResponse.data;

        //以京东分销商的身份申请退款
        Resaler resaler = Resaler.findOneByLoginName(JDGroupBuyUtil.JD_LOGIN_NAME);
        if (resaler == null){
            Logger.error("can not find the resaler by login name: %s", JDGroupBuyUtil.JD_LOGIN_NAME);
            finish(202, "can not find the jingdong resaler");return;
        }

        //处理退款
        List<CouponRequest> refundedCoupons = new ArrayList<>();
        for (CouponRequest coupon: sendOrderRefundRequest.coupons){
            ECoupon eCoupon = ECoupon.find("byECouponSn", coupon.couponId).first();
            if(eCoupon != null && eCoupon.order.orderNumber.equals(sendOrderRefundRequest.venderOrderId)){
                String ret = ECoupon.applyRefund(eCoupon, resaler.getId(), AccountType.RESALER);
                if(ret.equals("{\"error\":\"ok\"}")){
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
        finish(200, "success");
    }


    // 创建一百券订单
    private static Order createYbqOrder(SendOrderRequest sendOrderRequest) {
        Resaler resaler = Resaler.findOneByLoginName(JDGroupBuyUtil.JD_LOGIN_NAME);
        if (resaler == null){
            Logger.error("can not find the resaler by login name: %s", JDGroupBuyUtil.JD_LOGIN_NAME);
            finish(203, "can not find the jingdong resaler");return null;
        }
        Order ybqOrder = Order.createConsumeOrder(resaler.getId(), AccountType.RESALER);
        ybqOrder.save();
        try {
            models.sales.Goods goods = models.sales.Goods.find("byId", sendOrderRequest.venderTeamId).first();
            if(goods == null){
                Logger.info("goods not found: %s", sendOrderRequest.venderTeamId);
                finish(204, "can not find goods: " + sendOrderRequest.venderTeamId); return null;
            }
            if(goods.originalPrice.compareTo(sendOrderRequest.teamPrice) > 0){
                Logger.info("invalid yhd productPrice: %s", sendOrderRequest.teamPrice);
                finish(205, "invalid product price: " + sendOrderRequest.teamPrice); return null;
            }

            OrderItems uhuilaOrderItem  = ybqOrder.addOrderItem(
                    goods,
                    sendOrderRequest.count,
                    sendOrderRequest.mobile,
                    sendOrderRequest.teamPrice,
                    sendOrderRequest.teamPrice );
            uhuilaOrderItem.save();
            if(goods.materialType.equals(MaterialType.REAL)){
                ybqOrder.deliveryType = DeliveryType.SMS;
            }else if (goods.materialType.equals(MaterialType.ELECTRONIC)) {
                ybqOrder.deliveryType = DeliveryType.LOGISTICS;
            }
        } catch (NotEnoughInventoryException e) {
            Logger.info("inventory not enough");
            finish(206, "inventory not enough"); return null;
        }

        ybqOrder.createAndUpdateInventory();
        ybqOrder.accountPay = ybqOrder.needPay;
        ybqOrder.discountPay = BigDecimal.ZERO;
        ybqOrder.payMethod = PaymentSource.getBalanceSource().code;
        ybqOrder.payAndSendECoupon();
        ybqOrder.save();

        return ybqOrder;
    }

    private static void finish(int resultCode, String resultMessage){
        renderArgs.put("resultCode", resultCode);
        renderArgs.put("resultMessage", resultMessage);
        renderTemplate("jingdong/groupbuy/response/main.xml");
    }
}
