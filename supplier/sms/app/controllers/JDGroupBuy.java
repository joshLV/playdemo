package controllers;

import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.jingdong.JDGroupBuyUtil;
import models.jingdong.groupbuy.JDRequest;
import models.jingdong.groupbuy.SendOrder;
import models.order.*;
import models.resale.Resaler;
import models.sales.MaterialType;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.mvc.Before;
import play.mvc.Controller;
import play.templates.Template;
import play.templates.TemplateLoader;

import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 * Date: 12-9-28
 */
public class JDGroupBuy extends Controller{
    public static String JD_LOGIN_NAME = Play.configuration.getProperty("jingdong.resaler_login_name", "jingdong");
    public static String DATE_FORMAT = "yyy-MM-dd HH:mm:ss";

    public static String PHONE_REGEX = "^1[3,5,8]\\d{9}$";
    @Before
    public static void baseResponse(){
        renderArgs.put("version", "1.0");
        renderArgs.put("venderId", JDGroupBuyUtil.VENDER_ID);
        renderArgs.put("encrypt", "true");
        renderArgs.put("zip","false");
    }

    public static void sendOrder(){
        String restXml = request.body.toString();

        //解析请求
        JDRequest<SendOrder> sendOrderJDRequest = new JDRequest<>();
        if(!sendOrderJDRequest.parse(restXml, new SendOrder())){
            //解析失败
            finish(201, "parse send_order request xml error"); return;
        }
        SendOrder sendOrder = sendOrderJDRequest.data;

        //检查并保存此新请求
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId",
                OuterOrderPartner.JD, sendOrder.jdOrderId).first();
        //如果找不到该orderCode的订单，说明还没有新建，则新建一个
        if(outerOrder == null){
            outerOrder = new OuterOrder();
            outerOrder.partner = OuterOrderPartner.JD;
            outerOrder.status = OuterOrderStatus.ORDER_COPY;
            outerOrder.orderId = sendOrder.jdOrderId;
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
            Order ybqOrder = createYbqOrder(sendOrder);
            outerOrder.status = OuterOrderStatus.ORDER_DONE;
            outerOrder.ybqOrder = ybqOrder;
            outerOrder.save();
        }

        if(outerOrder.status != OuterOrderStatus.ORDER_CANCELED){
            Template template = TemplateLoader.load("JDGroupBuy/sendOrder.xml");
            Map<String, Object> params = new HashMap<>();
            params.put("sendOrder", sendOrder);
            params.put("ybqOrder", outerOrder.ybqOrder);
            renderArgs.put("data", template.render(params));
            finish(200, "success");
        }else {
            finish(207, "order canceled");
        }
    }

    public static void sendSms(){
        String restXml = request.body.toString();
    }

    private static void generateYibaiquanOrder(SendOrder sendOrder) {
        Order ybqOrder = new Order();

    }


    // 创建一百券订单
    private static Order createYbqOrder(SendOrder sendOrder) {
        Resaler resaler = Resaler.findOneByLoginName(JD_LOGIN_NAME);
        if (resaler == null){
            Logger.error("can not find the resaler by login name: %s", JD_LOGIN_NAME);
            finish(203, "can not find the jingdong resaler");return null;
        }
        Order ybqOrder = Order.createConsumeOrder(resaler.getId(), AccountType.RESALER);
        ybqOrder.save();
        try {
            models.sales.Goods goods = models.sales.Goods.find("byId", sendOrder.venderTeamId).first();
            if(goods == null){
                Logger.info("goods not found: %s", sendOrder.venderTeamId);
                finish(204, "can not find goods: " + sendOrder.venderTeamId); return null;
            }
            if(goods.originalPrice.compareTo(sendOrder.teamPrice) > 0){
                Logger.info("invalid yhd productPrice: %s", sendOrder.teamPrice);
                finish(205, "invalid product price: " + sendOrder.teamPrice); return null;
            }

            OrderItems uhuilaOrderItem  = ybqOrder.addOrderItem(
                    goods,
                    sendOrder.count,
                    sendOrder.mobile,
                    sendOrder.teamPrice,
                    sendOrder.teamPrice );
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
        renderTemplate("JDGroupBuy/main.xml");
    }
}
