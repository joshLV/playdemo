package controllers;

import models.order.*;
import play.mvc.Controller;
import java.math.BigDecimal;
import thirdpart.alipay.util.AlipayNotify;

import java.util.*;

public class OrderResult extends Controller {

    public static void alipayReturn(
            String trade_status, String buyer_email, 
            String trade_no, String out_trade_no, 
            BigDecimal total_fee, String subject, String body) {

        org.apache.log4j.Logger logger = 
            org.apache.log4j.Logger.getLogger("thirdpart.alipay");

        logger.info("alipay_return:"    +
                "交易状态:"             + trade_status  + "," + 
                "买家账号:"             + buyer_email   + "," +
                "交易号:"               + trade_no      + "," +
                "订单号:"               + out_trade_no  + "," + 
                "总金额:"               + total_fee     + "," +
                "商品名称:"             + subject       + "," +
                "备注信息:"             + body);

        //获取支付宝GET过来反馈信息
        Map<String,String> verifyParams = new HashMap<String,String>();
        Map<String, String[]> paramsMap = params.all();
        for (Iterator iter = params.all().keySet().iterator();iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = params.getAll(name);
            String valueStr = ""; 
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                    : valueStr + values[i] + ",";
            }   
            //乱码解决，这段代码在出现乱码时使用。
            //如果mysign和sign不相等也可以使用这段代码转化
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            if(name.equals("body")){
                continue;
            }
            verifyParams.put(name, valueStr);
        }   

        String errorMessage = null;
        models.order.Orders order =  null;
        //验证通知结果
        
        if(!AlipayNotify.verify(verifyParams)){
            errorMessage = "支付宝参数非法，请您稍后再试";

        }else if(trade_status == null){
            errorMessage = "订单状态未知，请稍后再试";

        }else if(!trade_status.equals("TRADE_FINISHED") && 
                !trade_status.equals("TRADE_SUCCESS")){
            errorMessage = "订单返回状态异常，请您稍后再试";

        }else if(out_trade_no == null || total_fee == null){
            errorMessage = "订单信息有误，请您稍后再试";

        }else {
            order = models.order.Orders
                .find("byOrderNumber",out_trade_no)
                .first();
            if(order == null){
                errorMessage = "无此订单，请您稍后再试";

            }else if( total_fee.compareTo(order.needPay) < 0){
                errorMessage = "订单金额不符，请您稍后再试";

            }else if(order.status != OrderStatus.PAID){
                errorMessage = "等待支付返回，请您稍后再试";
            }
        }

        renderTemplate("OrderResult/index.html", order, errorMessage);

    }

}
