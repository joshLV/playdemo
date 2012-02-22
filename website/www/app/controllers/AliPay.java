package controllers;

import models.consumer.*;
import models.order.*;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;
import java.math.BigDecimal;
import thirdpart.alipay.util.AlipayNotify;

import java.util.*;
import controllers.modules.cas.SecureCAS;

public class AliPay extends Controller {

    public static void notify(
            String trade_status, String buyer_email, 
            String trade_no, String out_trade_no, 
            BigDecimal total_fee, String subject, String body) {

        org.apache.log4j.Logger logger = 
            org.apache.log4j.Logger.getLogger("thirdpart.alipay");

        logger.info("alipay_notify:"    +   
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
            System.out.println("alipay_notify: name:" + name + ",value:" + valueStr);
            if(name.equals("body")){
                continue;
            }
            verifyParams.put(name, valueStr);
        }   

        //验证通知结果
        if(!AlipayNotify.verify(verifyParams)){
            logger.info("alipay_notify:支付宝参数验证失败");
            return;
        }
        if(trade_status !=null && 
                !trade_status.equals("TRADE_FINISHED") && 
                !trade_status.equals("TRADE_SUCCESS")){
            logger.info("alipay_notify:支付结果异常");
            return;
                }

        if(out_trade_no == null || total_fee == null){
            logger.info("alipay_notify:订单编号或订单金额非法");
            return;
        }

        models.order.Orders order = 
            models.order.Orders.find("byOrderNumber",out_trade_no).first();
        if(order == null){
            logger.info("alipay_notify:查无此订单:" + out_trade_no);
            return;
        }

        if( total_fee.compareTo(order.needPay) < 0){
            logger.info("alipay_notify:订单金额不符:" + out_trade_no);
            return;
        }
        if(!order.status.equals(OrderStatus.UNPAID.toString())){
            logger.info("alipay_notify:订单已被处理:" + out_trade_no);
            return;
        }
        order.status = OrderStatus.PAID.toString();
        order.paidAt = new Date();
        order.save();
    }


    public static void alipayNotify(){

    }
}
