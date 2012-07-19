package controllers;

import models.accounts.AccountType;
import models.api.Utils;
import models.api.order.ExternalOrder;
import models.order.NotEnoughInventoryException;
import models.order.Order;
import models.resale.Resaler;
import models.resale.ResalerStatus;
import models.sales.Goods;
import play.Logger;
import play.mvc.Controller;

import java.math.BigDecimal;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author likang
 */
public class OrderAPI extends Controller{
    public static void newOrder(String serial_number, long goods_id,
                                int number, String mobile, String app_key, String sign){
        Logger.info("order api %s:%s %s:%s %s:%s %s:%s %s:%s %s:%s",
                "serial_number", serial_number, "goods_id", goods_id, "number", number,
                "mobile", mobile, "app_key", app_key, "sign", sign);
        //检查参数
        if(isBlank(serial_number)){
            Logger.error("invalid serial_number: %s", serial_number);
            renderJSON("");//todo
        }

        if(isBlank(mobile)){
            Logger.error("invalid mobile: %s", serial_number);
            renderJSON("");//todo
        }

        if(isBlank(app_key)) {
            Logger.error("invalid app_key: %s", app_key);
            renderJSON("");//todo
        }

        if(isBlank(sign)){
            Logger.error("invalid sign: %s", sign);
            renderJSON("");//todo
        }

        //定位请求者
        Resaler resaler = Resaler.find("byKey", app_key).first();
        if(resaler == null || resaler.status != ResalerStatus.APPROVED){
            Logger.error("unavailable app_key: ", app_key);
            renderJSON("");//todo
        }

        //校验参数
        SortedMap<String, String> params = new TreeMap<>();
        params.put("serial_number", serial_number);
        params.put("goods_id", String.valueOf(goods_id));
        params.put("number", String.valueOf(number));
        params.put("mobile", mobile);
        if(!Utils.validSign(params, app_key, resaler.appSecretKey, sign)){//todo
            Logger.error("wrong sign: ", sign);
            renderJSON("");//todo
        }

        //
        Goods goods = Goods.findById(goods_id);
        if(goods == null){
            Logger.error("can not find goods: %s", goods_id);
            renderJSON("");//todo
        }

        Order order = Order.find("byExtRequestSN", serial_number).first();
        if(order != null){
            renderJSON("");//todo repeated request
        }
        order = Order.createConsumeOrder(resaler.getId(), AccountType.RESALER);
        BigDecimal resalerPrice = goods.getResalePrice(resaler.level);
        try{
            order.addOrderItem(goods, number, mobile, resalerPrice, resalerPrice);
        } catch (NotEnoughInventoryException e) {
            renderJSON("");//todo inventory not enough
        }
        order.createAndUpdateInventory();
        order.payAndSendECoupon();

        //todo

        ExternalOrder externalOrder = new ExternalOrder();
        externalOrder.externalNumber = serial_number;
        externalOrder.order = order;
        externalOrder.goods = goods;
        externalOrder.number = number;
        externalOrder.mobile = mobile;
        externalOrder.resaler = resaler;
        externalOrder.save();
        renderJSON("");//todo
    }

    public static void refund(){

    }

    private static boolean isBlank(String str){
        return str == null || str.trim().equals("");
    }
}
