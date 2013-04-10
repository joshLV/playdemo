package models.payment.alipay;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import models.payment.PaymentFlow;
import models.payment.PaymentUtil;
import play.Logger;
import thirdpart.alipay.services.AlipayService;
import thirdpart.alipay.util.AlipayNotify;

/**
 * @author likang
 *         Date: 12-3-16
 */
public class AliPaymentFlow extends PaymentFlow {


    @Override
    public String getRequestForm(String orderNumber, String description, BigDecimal fee,
                                 String subPaymentCode, String remoteIp, String ext){
        //必填参数//

        //请与贵网站订单系统中的唯一订单号匹配
        String out_trade_no = orderNumber;
        //订单名称，显示在支付宝收银台里的“商品名称”里，显示在支付宝的交易管理的
        //“商品名称”的列表里。
        String subject = description;
        //订单总金额，显示在支付宝收银台里的“应付总额”里
        String total_fee = fee.toString();

        //订单描述、订单详细、订单备注，显示在支付宝收银台里的“商品描述”里
        String body = "优惠啦测试商品描述";

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
        String show_url = "http://www.yibaiquan.com/";
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


    @Override
    public Map<String, String> parseRequestParams(Map<String, String[]> requestParams){
        Map<String, String> result = new HashMap<>();
        for(Map.Entry<String, String[]> entry : requestParams.entrySet()){
            String[] values = entry.getValue();
            StringBuilder value = new StringBuilder();
            for (int i = 0; i < values.length; i++){
                String v = (i == values.length -1) ? values[i] : values[i] + ",";
                value.append(v);
            }

            result.put(entry.getKey(), value.toString());
        }
        return result;
    }

    /**
     * 支付宝服务器回掉支付结果
     */
    @Override
    public Map<String, String> notify(Map<String, String[]> requestParams){
        Map<String, String> result = new HashMap<>();
        Map<String, String> params = parseRequestParams(requestParams);
        result.put(PAYMENT_CODE, PaymentUtil.PARTNER_CODE_ALIPAY);

        if(AlipayNotify.verify(params)){
            Logger.info("alipay: verify success");
            result.put(VERIFY_RESULT, VERIFY_RESULT_OK);
            String orderNumber = params.get("out_trade_no");
            result.put(ORDER_NUMBER, orderNumber);
            String totalFee = params.get("total_fee");
            result.put(TOTAL_FEE, totalFee);
            result.put(SUCCESS_INFO, "success");
        }else {
            Logger.error("alipay: verify failed");
            result.put(VERIFY_RESULT, VERIFY_RESULT_ERROR);
            result.put(SUCCESS_INFO, "failed");
        }
        return result;
    }

    @Override
    public Map<String, String> urlReturn(Map<String, String[]> requestParams){
        return notify(requestParams);
    }
}
