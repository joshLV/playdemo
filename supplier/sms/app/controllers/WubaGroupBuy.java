package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.order.*;
import models.resale.Resaler;
import models.sales.GoodsDeployRelation;
import models.sales.MaterialType;
import models.wuba.WubaUtil;
import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Controller;

import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author likang
 *         Date: 12-11-23
 */
public class WubaGroupBuy extends Controller {
    public static String PHONE_REGEX = "^1[3,5,8]\\d{9}$";

    /**
     * 新订单通知
     * @param param
     */
    public static void newOrder(String param) {
        Map<String, String> allParams = request.params.allSimple();
        allParams.remove("body");
        Logger.info("wuba request: \n%s", new Gson().toJson(allParams));
        JsonObject orderJson = WubaUtil.parseRequest(param);
        Logger.info("wuba request decrypted: \n%s", orderJson.toString());

        Map<String, Object> result = new HashMap<>();
        putStatusAndMsg(result, "10000", "成功");
        Long orderId, outerGroupId;
        BigDecimal productPrize;
        int productNum;
        String userPhone;

        try {
            orderId = orderJson.get("orderId").getAsLong();
            productPrize = orderJson.get("prodPrice").getAsBigDecimal();
            productNum = orderJson.get("prodCount").getAsInt();
            userPhone = orderJson.get("mobile").getAsString();
            outerGroupId = orderJson.get("groupbuyIdThirdpart").getAsLong();
        }catch (Exception e) {
            Logger.info("wuba request failed: wrong params");
            putStatusAndMsg(result, "10201", "参数解析错误");
            finish(result);return;
        }


        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId",
                OuterOrderPartner.WUBA, orderId).first();
        //如果找不到该orderCode的订单，说明还没有新建，则新建一个
        if(outerOrder == null){
            outerOrder = new OuterOrder();
            outerOrder.partner = OuterOrderPartner.WUBA;
            outerOrder.status = OuterOrderStatus.ORDER_COPY;
            outerOrder.message = orderJson.toString();
            outerOrder.save();
            try{ // 将订单写入数据库
                JPA.em().flush();
            }catch (Exception e){ // 如果写入失败，说明 已经存在一个相同的orderCode 的订单，则放弃
                putStatusAndMsg(result, "10100", "并发的订单请求");
                Logger.info("wuba request failed: concurrency request");
                finish(result); return;
            }
        }else {
            outerOrder.message = orderJson.toString();
        }

        outerOrder.orderId = orderId;
        outerOrder.save();
        //检查订单数量
        if(productNum <= 0 || productPrize.compareTo(BigDecimal.ZERO) < 0 || !checkPhone(userPhone)){
            putStatusAndMsg(result, "20210", "输入参数错误");
            Logger.info("wuba request failed: invalid params");
            finish(result);return;
        }

        try{
            // 尝试申请一个行锁
            JPA.em().refresh(outerOrder, LockModeType.PESSIMISTIC_WRITE);
        }catch (PersistenceException e){
            //没拿到锁 放弃
            putStatusAndMsg(result, "10100", "并发的订单请求");
            Logger.info("wuba request failed: concurrency request");
            finish(result); return;
        }

        if (outerOrder.status == OuterOrderStatus.ORDER_COPY){
            Order ybqOrder = createYbqOrder(outerGroupId, productPrize, productNum, userPhone, result);
            if(!result.get("status").equals("10000")){
                finish(result);return;
            }else if(ybqOrder != null){
                outerOrder.status = OuterOrderStatus.ORDER_SYNCED;
                outerOrder.ybqOrder = ybqOrder;
                outerOrder.save();
            }
        }else if(outerOrder.status != OuterOrderStatus.ORDER_SYNCED){
            putStatusAndMsg(result, "10100", "订单状态错误");
            Logger.info("wuba request failed: wrong order status");
            finish(result);return;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", outerOrder.orderId);
        data.put("orderIdThirdpart", outerOrder.ybqOrder.orderNumber);
        List<Map<String, Object>> tickets = new ArrayList<>();
        List<ECoupon> eCoupons = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
        for(ECoupon coupon : eCoupons) {
            coupon.partner = ECouponPartner.WUBA;
            coupon.save();

            Map<String, Object> ticket = new HashMap<>();
            ticket.put("ticketId", coupon.id);
            ticket.put("ticketCode", coupon.eCouponSn);
            ticket.put("ticketPass", "");
            ticket.put("ticketCount", 1);
            ticket.put("createTime", simpleDateFormat.format(coupon.createdAt));
            ticket.put("endTime", simpleDateFormat.format(coupon.expireAt));
            tickets.add(ticket);
        }
        data.put("tickets", tickets);
        result.put("data", data);
        Logger.info("wuba request success. %s tickets generated", tickets.size());
        finish(result);
    }

    public static void refund(String param) {
        Map<String, String> allParams = request.params.allSimple();
        allParams.remove("body");
        Logger.info("wuba request: \n%s", new Gson().toJson(allParams));
        JsonObject refundJson = WubaUtil.parseRequest(param);
        Logger.info("wuba request: \n%s", refundJson.toString());

        Long ticketId;
        Long orderId;
        String reason;
        String status;
        Map<String, Object> result = new HashMap<>();
        putStatusAndMsg(result, "10000", "成功");
        try{
            ticketId = refundJson.get("ticketId").getAsLong();
            orderId = refundJson.get("orderId").getAsLong();
            reason = refundJson.get("reason").getAsString();
            status = refundJson.get("status").getAsString();
        }catch (Exception e) {
            putStatusAndMsg(result, "10201", "参数错误");
            finish(result); return;
        }
        ECoupon coupon = ECoupon.findById(ticketId);
        if (coupon == null || !coupon.order.id.equals(orderId)) {
            putStatusAndMsg(result, "10202", "券不存在");
            finish(result); return;
        }
        if(!status.equals("10") && !status.equals("11")) {
            putStatusAndMsg(result, "10100", "该券状态无法退款");
            finish(result); return;
        }

        Resaler resaler = Resaler.findOneByLoginName(Resaler.WUBA_LOGIN_NAME);
        if (resaler == null){
            Logger.error("can not find the resaler by login name: %s", Resaler.WUBA_LOGIN_NAME);
            putStatusAndMsg(result, "10100", "未找到58账户");return;
        }

        String ret = ECoupon.applyRefund(coupon, resaler.getId(), AccountType.RESALER);
        if(!ret.equals(ECoupon.ECOUPON_REFUND_OK)){
            putStatusAndMsg(result, "10100", "退款失败");
            finish(result);
        }else{
            finish(result);
        }
    }

    // 创建一百券订单
    private static Order createYbqOrder(Long outerGroupId, BigDecimal productPrize,
                                        Integer productNum, String userPhone, Map<String, Object> result) {
        Resaler resaler = Resaler.findOneByLoginName(Resaler.WUBA_LOGIN_NAME);
        if (resaler == null){
            Logger.error("can not find the resaler by login name: %s", Resaler.WUBA_LOGIN_NAME);
            putStatusAndMsg(result, "10100", "未找到58账户");
            return null;
        }
        Order ybqOrder = Order.createConsumeOrder(resaler.getId(), AccountType.RESALER);
        ybqOrder.save();
        try {
            GoodsDeployRelation goodsDeployRelation = GoodsDeployRelation.find("byLinkId", outerGroupId).first();
            if (goodsDeployRelation == null || goodsDeployRelation.goods == null){
                Logger.info("can not find goodsDeployRelation: %s", outerGroupId);
                putStatusAndMsg(result, "10202", "未找到商品对应关系");
                return null;
            }
            models.sales.Goods goods =  goodsDeployRelation.goods;
            if(goods == null){
                putStatusAndMsg(result, "10100", "未找到商品");
                Logger.info("goods not found: %s", outerGroupId);
                return null;
            }
            if(goods.originalPrice.compareTo(productPrize) > 0){
                Logger.info("invalid yhd productPrice: %s", productPrize);
                putStatusAndMsg(result, "10100", "价格非法");
                return null;
            }

            OrderItems uhuilaOrderItem  = ybqOrder.addOrderItem(
                    goods, productNum, userPhone, productPrize, productPrize );
            uhuilaOrderItem.save();
            if(goods.materialType.equals(MaterialType.REAL)){
                ybqOrder.deliveryType = DeliveryType.SMS;
            }else if (goods.materialType.equals(MaterialType.ELECTRONIC)) {
                ybqOrder.deliveryType = DeliveryType.LOGISTICS;
            }
        } catch (NotEnoughInventoryException e) {
            Logger.info("enventory not enough");
            putStatusAndMsg(result, "10100", "价格非法");
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

    private static void putStatusAndMsg(Map<String, Object> result, String status, String msg) {
        result.put("status", status);
        result.put("msg", msg);
    }

    private static void finish(Map<String, Object> result) {
        Gson gson = new Gson();
        if(result.get("status").equals("10000")) {
            Map<String, Object> data = (Map<String, Object>)result.get("data");
            String dataJson = gson.toJson(data);
            result.put("data", WubaUtil.encryptMessage(dataJson));
        }
        renderJSON(gson.toJson(result));
    }

    private static boolean checkPhone(String phone){
        if(phone == null){
            return false;
        }
        Pattern pattern = Pattern.compile(PHONE_REGEX);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

}
