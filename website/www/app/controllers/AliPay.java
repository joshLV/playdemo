package controllers;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import models.accounts.PaymentCallbackLog;
import models.accounts.TradeBill;
import models.accounts.util.TradeUtil;
import models.order.*;
import models.order.Orders;
import models.sales.MaterialType;
import models.sms.SMSUtil;
import play.Logger;
import play.mvc.Controller;
import thirdpart.alipay.util.AlipayNotify;

public class AliPay extends Controller {

    public static void notify(
            String trade_status, String buyer_email,
            String trade_no, String out_trade_no,
            BigDecimal total_fee, String subject, String body) {


        String log = "alipay_notify:" +
                "交易状态:" + trade_status + "," +
                "买家账号:" + buyer_email + "," +
                "交易号:" + trade_no + "," +
                "订单号:" + out_trade_no + "," +
                "总金额:" + total_fee + "," +
                "商品名称:" + subject + "," +
                "备注信息:" + body;
        Logger.info(log);

        PaymentCallbackLog callbackLog =
                new PaymentCallbackLog(buyer_email, "alipay", out_trade_no, total_fee, trade_status, log);

        //获取支付宝GET过来反馈信息
        Map<String, String> verifyParams = new HashMap<String, String>();
        Map<String, String[]> paramsMap = params.all();
        for (String name : params.all().keySet()) {
            String[] values = params.getAll(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。
            //如果mysign和sign不相等也可以使用这段代码转化
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            if (name.equals("body")) {
                continue;
            }
            verifyParams.put(name, valueStr);
        }

        //验证通知结果
        if (!AlipayNotify.verify(verifyParams)) {
            Logger.error("alipay_notify:支付宝参数验证失败");
            callbackLog.status = "alipay_verify_failed";
        } else if (trade_status != null &&
                !trade_status.equals("TRADE_FINISHED") &&
                !trade_status.equals("TRADE_SUCCESS")) {
            Logger.error("alipay_notify:支付结果异常");
        } else if (out_trade_no == null || total_fee == null) {
            Logger.error("alipay_notify:订单编号或订单金额非法");
            callbackLog.status = "invalid_trade";
        } else {
            Orders order =
                    Orders.find("byOrderNumber", out_trade_no).first();
            if (order == null) {
                Logger.error("alipay_notify:查无此订单:" + out_trade_no);
                callbackLog.status = "invalid_order";
            } else if (total_fee.compareTo(order.needPay) < 0) {
                Logger.error("alipay_notify:订单金额不符:" + out_trade_no);
                callbackLog.status = "invalid_amount";
            } else if (order.status != OrderStatus.UNPAID) {
                Logger.error("alipay_notify:订单已被处理:" + out_trade_no);
                callbackLog.status = "processed";
            } else {
                Long tradeId = order.payRequestId;
                TradeBill tradeBill = TradeBill.findById(tradeId);
                if(tradeBill != null){
                    //最终所有条件满足
                    TradeUtil.success(tradeBill);
                    order.paid();

                }else {
                    callbackLog.status = "no_trade_found";
                }
            }
        }
        callbackLog.save();
    }
}
