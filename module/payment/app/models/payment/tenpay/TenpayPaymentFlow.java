package models.payment.tenpay;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import models.payment.PaymentFlow;
import models.payment.PaymentUtil;
import thirdpart.tenpay.client.ClientResponseHandler;
import thirdpart.tenpay.client.TenpayHttpClient;
import thirdpart.tenpay.util.TenpayUtil;
import play.Logger;

/**
 * 财付通支付接口
 * 
 * @author yanjy
 *
 */
public class TenpayPaymentFlow extends PaymentFlow {

    /**
     * 返回跳转信息.
     *
     * @param orderNumber 订单号
     * @param description 订单描述
     * @param fee 订单金额
     * @param remoteIp 客户ID
     * @return 跳转信息
     */
    @Override
    public String getRequestForm(String orderNumber, String description, BigDecimal fee, String subPaymentCode,
                                 String remoteIp, String ext) {
        SortedMap<String, String> params = new TreeMap<>();
        String orderAmount= fee.multiply(new BigDecimal(100)).setScale(0,BigDecimal.ROUND_HALF_UP).toString();

        //-----------------------------
        //设置支付参数
        //-----------------------------
        params.put("partner", TenpayUtil.APP_ID);		        //商户号
        params.put("out_trade_no", orderNumber);		//商家订单号
        params.put("total_fee", orderAmount);			        //商品金额,以分为单位
        params.put("return_url", TenpayUtil.RETURN_URL);		    //交易完成后跳转的URL
        params.put("notify_url", TenpayUtil.NOTIFY_URL);		    //接收财付通通知的URL
        params.put("body", description);	                    //商品描述
        params.put("bank_type", "DEFAULT");		    //银行类型(中介担保时此参数无效)
        params.put("spbill_create_ip", remoteIp);   //用户的公网ip，不是商户服务器IP
        params.put("fee_type", "1");                    //币种，1人民币
        params.put("subject", description);              //商品名称(中介交易时必填)

        //系统可选参数
        params.put("sign_type", "MD5");                //签名类型,默认：MD5
        params.put("service_version", "1.0");			//版本号，默认为1.0
        params.put("input_charset", TenpayUtil.getCharacterEncoding());            //字符编码
        params.put("sign_key_index", "1");             //密钥序号


        //业务可选参数
        params.put("attach", "");                      //附加数据，原样返回
        params.put("product_fee", orderAmount);                 //商品费用，必须保证transport_fee + product_fee=total_fee
        params.put("transport_fee", "0");               //物流费用，必须保证transport_fee + product_fee=total_fee
        params.put("time_start", TenpayUtil.getCurrTime());            //订单生成时间，格式为yyyymmddhhmmss
        params.put("time_expire", "");                 //订单失效时间，格式为yyyymmddhhmmss
        params.put("buyer_id", "");                    //买方财付通账号
        params.put("goods_tag", "");                   //商品标记
        params.put("trade_mode", "1");                 //交易模式，1即时到账(默认)，2中介担保，3后台选择（买家进支付中心列表选择）
        params.put("transport_desc", "");              //物流说明
        params.put("trans_type", "1");                  //交易类型，1实物交易，2虚拟交易
        params.put("agentid", "");                     //平台ID
        params.put("agent_type", "");                  //代理模式，0无代理(默认)，1表示卡易售模式，2表示网店模式
        params.put("seller_id", "");                   //卖家商户号，为空则等同于partner


        TenpayUtil.addSign(params);

        StringBuilder sbHtml = new StringBuilder();
        sbHtml.append("<form id=\"tenPay\" name=\"tenPay\" action=\"" + TenpayUtil.PAY_GATE_URL + "\" method=\"get\" >");

        for(Map.Entry<String, String> entry : params.entrySet()){
            sbHtml.append("<input type=\"hidden\" name=\"")
                  .append(entry.getKey())
                  .append("\" value=\"")
                  .append(entry.getValue())
                  .append("\"/>");
        }

        sbHtml.append("</form><script>document.forms['tenPay'].submit();</script>");

        return sbHtml.toString();

    }



    /**
     * 验证支付返回参数是否合法.
     * 同时返回三个String参数:
     *   verify_result   是否验证成功, true/false
     *   order_number    订单号
     *   total_fee       支付金额
     *
     * @param requestParams 请求
     * @return 支付返回参数是否合法
     */
    @Override
    public Map<String, String> notify(Map<String, String[]> requestParams) {
        Map<String, String> result = new HashMap<>();
        Map<String, String> params = parseRequestParams(requestParams);
        result.put(VERIFY_RESULT, VERIFY_RESULT_ERROR);
        result.put(PAYMENT_CODE, PaymentUtil.PARTNER_CODE_TENPAY);

        if (TenpayUtil.isTenpaySign(new TreeMap<>(params))){
            //通知id
            String notify_id = params.get("notify_id");
            //通信对象
            TenpayHttpClient httpClient = new TenpayHttpClient();
            //应答对象
            ClientResponseHandler queryRes = new ClientResponseHandler();

            //通过通知ID查询，确保通知来至财付通
            SortedMap<String,String> verifyParams = new TreeMap<>();
            verifyParams.put("partner", TenpayUtil.APP_ID);
            verifyParams.put("notify_id", notify_id);

            String requestUrl = TenpayUtil.getRequestUrl(verifyParams, TenpayUtil.VERIFY_GATE_URL);

            //通信对象
            httpClient.setTimeOut(5);
            //设置请求内容
            httpClient.setReqContent(requestUrl);
            //后台调用
            if(httpClient.call()) {
                //设置结果参数
                try {
                    queryRes.setContent(httpClient.getResContent());
                } catch (Exception e) {
                    Logger.error("tenpay notify: setContent failed");
                    return result;
                }
                Logger.info("tenpay: 验证ID返回字符串:" + httpClient.getResContent());
                queryRes.setKey(TenpayUtil.SECRET_KEY);

                //获取id验证返回状态码，0表示此通知id是财付通发起
                String retcode = queryRes.getParameter("retcode");

                //商户订单号
                String outTradeNo = params.get("out_trade_no");
                //金额,以分为单位
                String totalFee = params.get("total_fee");
                String trade_state = params.get("trade_state");
                //交易模式，1即时到账，2中介担保
                String trade_mode = params.get("trade_mode");

                result.put(ORDER_NUMBER, outTradeNo);
                BigDecimal fee = new BigDecimal(totalFee);
                result.put(TOTAL_FEE, fee.divide(new BigDecimal("100"), 2, RoundingMode.HALF_DOWN).toString());
                result.put(SUCCESS_INFO, "failed");
                //判断签名及结果
                if(queryRes.isTenpaySign()&& "0".equals(retcode)){
                    if("1".equals(trade_mode)){       //即时到账
                        if( "0".equals(trade_state)){
                            result.put(VERIFY_RESULT, VERIFY_RESULT_OK);
                            result.put(SUCCESS_INFO, "success");
                        }
                    }
                }
                Logger.info("tenpay notify: retcode:" + retcode + ";trade_state:" + trade_state + ";trade_mode:" + trade_mode);
            }else {
                Logger.error("tenpay notify: httpClient call failed");
            }
        }else {
            Logger.error("tenpay notify: isTenpaySign failed");
        }

        return result;
    }

    @Override
    public Map<String, String> urlReturn(Map<String, String[]> requestParams){
        //创建支付应答对象
        Map<String, String> result = new HashMap<>();
        Map<String, String> params = parseRequestParams(requestParams);
        result.put(VERIFY_RESULT, VERIFY_RESULT_ERROR);
        result.put(PAYMENT_CODE, PaymentUtil.PARTNER_CODE_TENPAY);

        //判断签名
        if(TenpayUtil.isTenpaySign(new TreeMap<>(params))) {

            //商户订单号
            String outTradeNo = params.get("out_trade_no");
            result.put(ORDER_NUMBER, outTradeNo);

            //金额,以分为单位
            String totalFee = params.get("total_fee");
            BigDecimal fee = new BigDecimal(totalFee);
            result.put(TOTAL_FEE, fee.divide(new BigDecimal("100"), 2, RoundingMode.HALF_DOWN).toString());


            //支付结果
            String trade_state = params.get("trade_state");
            //交易模式，1即时到账，2中介担保
            String trade_mode = params.get("trade_mode");

            if("1".equals(trade_mode)){       //即时到账
                if( "0".equals(trade_state)){
                    result.put(VERIFY_RESULT, VERIFY_RESULT_OK);
                }
            }
            Logger.info("tenpay url return: trade_mode:" + trade_mode + ";trade_state:" + trade_state);
        }else {
            Logger.error("tenpay url return: isTenpaySign failed");
        }
        return result;
    }

}
