package models.payment;

import java.math.BigDecimal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import models.accounts.PaymentCallbackLog;
import models.accounts.TradeBill;
import models.order.OrderStatus;

import models.accounts.util.TradeUtil;

import models.order.Order;

import play.Logger;

import thirdpart.alipay.services.AlipayService;
import thirdpart.alipay.util.AlipayNotify;

/**
 * @author likang
 *         Date: 12-3-16
 */
public class AliPaymentFlow implements PaymentFlow{
    public String generateForm(Order order){
        //必填参数//

        //请与贵网站订单系统中的唯一订单号匹配
        String out_trade_no = order.orderNumber;
        //订单名称，显示在支付宝收银台里的“商品名称”里，显示在支付宝的交易管理的
        //“商品名称”的列表里。
        String subject = null;
        if(order.orderItems.size() == 1){
            subject = order.orderItems.get(0).goodsName ;
        }else if(order.orderItems.size() > 1){
            subject = order.orderItems.get(0).goodsName + "等商品";
        }
        //订单描述、订单详细、订单备注，显示在支付宝收银台里的“商品描述”里
        String body = "优惠啦测试商品描述";
        //订单总金额，显示在支付宝收银台里的“应付总额”里
        String total_fee = order.needPay.toString();


        //扩展功能参数——默认支付方式//

        //默认支付方式，取值见“即时到帐接口”技术文档中的请求参数列表
        String paymethod = "directPay";
        //默认网银代号，代号列表见“即时到帐接口”技术文档“附录”→“银行列表”
        String defaultbank = "";

        //扩展功能参数——防钓鱼//

        //防钓鱼时间戳
//        String anti_phishing_key  = AlipayService.query_timestamp();

        //获取客户端的IP地址，建议：编写获取客户端IP地址的程序
//        String exter_invoke_ip= "127.0.0.1";//request.getRemoteAddr();
        //注意：
        //1.请慎重选择是否开启防钓鱼功能
        //2.exter_invoke_ip、anti_phishing_key一旦被设置过，
        //那么它们就会成为必填参数
        //3.开启防钓鱼功能后，服务器、本机电脑必须支持远程XML解析，
        //请配置好该环境。
        //4.建议使用POST方式请求数据
        //示例：
        //anti_phishing_key = AlipayService.query_timestamp();
        //获取防钓鱼时间戳函数
        //exter_invoke_ip = "202.1.1.1";

        //扩展功能参数——其他///

        //自定义参数，可存放任何内容（除=、&等特殊字符外），不会显示在页面上
        String extra_common_param = "";
        //默认买家支付宝账号
        String buyer_email = "";
        //商品展示地址，要用http:// 格式的完整路径，不允许加?id=123这类自定义参数
        String show_url = "http://test.uhuila.com:19001/";
        //扩展功能参数——分润(若要使用，请按照注释要求的格式赋值)//

        //提成类型，该值为固定值：10，不需要修改
        String royalty_type = "";
        //提成信息集
        String royalty_parameters ="";
        //注意：
        //与需要结合商户网站自身情况动态获取每笔交易的各分润收款账号、各分润金额、各分润说明。最多只能设置10条
        //各分润金额的总和须小于等于total_fee
        //提成信息集格式为：收款方Email_1^金额1^备注1|收款方Email_2^金额2^备注2
        //示例：
        //royalty_type = "10"
        //royalty_parameters    = "111@126.com^0.01^分润备注一|222@126.com^0.01^分润备注二"

        //////////////////////////////////////////////////////////////////////////////////

        //把请求参数打包成数组
        Map<String, String> sParaTemp = new HashMap<String, String>();
        sParaTemp.put("payment_type", "1");
        sParaTemp.put("out_trade_no", out_trade_no);
        sParaTemp.put("subject", subject);
//        sParaTemp.put("body", body);    //play 无法方便的获得传回的body参数，因此干脆不请求此参数
        sParaTemp.put("total_fee", total_fee);
        sParaTemp.put("show_url", show_url);
        sParaTemp.put("paymethod", paymethod);
        sParaTemp.put("defaultbank", defaultbank);
//        sParaTemp.put("anti_phishing_key", anti_phishing_key);
//        sParaTemp.put("exter_invoke_ip", exter_invoke_ip);
        sParaTemp.put("extra_common_param", extra_common_param);
        sParaTemp.put("buyer_email", buyer_email);
        sParaTemp.put("royalty_type", royalty_type);
        sParaTemp.put("royalty_parameters", royalty_parameters);

        //构造函数，生成请求URL
        return AlipayService.create_direct_pay_by_user(sParaTemp);
    } 
    /**
     * 校验 http 返回参数
     */
    @Override
    public boolean verifyParams(Map<String, String[]> params){
        Map<String,String> verifyParams = new HashMap<String,String>();
        for (Iterator iter = params.keySet().iterator();iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = params.get(name);
            String valueStr = ""; 
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                    : valueStr + values[i] + ",";
            }   
            //乱码解决，这段代码在出现乱码时使用。
            //如果mysign和sign不相等也可以使用这段代码转化
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            //Play 会将body参数改成所有参数的组合，因此过滤掉
            if(name.equals("body")){
                continue;
            }
            verifyParams.put(name, valueStr);
        }   
    	
        return AlipayNotify.verify(verifyParams);
    }
    
    /**
     * 支付宝服务器回掉支付结果
     */
    public boolean paymentNotify(Map<String, String[]> params){

        String trade_status = params.get("trade_status") == null ? null : params.get("trade_status")[0];
        String buyer_email  = params.get("buyer_email") == null ? null : params.get("buyer_email")[0];
        String trade_no     = params.get("trade_no") == null ? null : params.get("trade_no")[0];
        String out_trade_no = params.get("out_trade_no") == null ? null : params.get("out_trade_no")[0];
        BigDecimal total_fee    = params.get("total_fee") == null ? null : new BigDecimal(params.get("total_fee")[0]);
        String subject      = params.get("subject") == null ? null : params.get("subject")[0];

        String log = "alipay_notify:" +
                "交易状态:" + trade_status + "," +
                "买家账号:" + buyer_email + "," +
                "交易号:" + trade_no + "," +
                "订单号:" + out_trade_no + "," +
                "总金额:" + total_fee + "," +
                "商品名称:" + subject;
        Logger.info(log);

        PaymentCallbackLog callbackLog =
                new PaymentCallbackLog(buyer_email, "alipay", out_trade_no, total_fee, trade_status, log);

        boolean success = false;
        boolean verifyResult = verifyParams(params);
        //验证通知结果
        if (!verifyResult) {
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
            Order order = Order.find("byOrderNumber", out_trade_no).first();
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
                    success = true;

                }else {
                    callbackLog.status = "no_trade_found";
                }
            }
        }
        callbackLog.save();
        return success;
    }   
}
