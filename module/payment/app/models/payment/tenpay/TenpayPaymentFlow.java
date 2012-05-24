package models.payment.tenpay;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import com.tenpay.api.NotifyQueryRequest;
import com.tenpay.api.NotifyQueryResponse;
import com.tenpay.api.PayRequest;
import models.payment.PaymentFlow;
import play.mvc.Http.Request;

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
     * @param remoteIp 客户Ip
     * @return 跳转信息
     */
    @Override
    public String getRequestForm(String orderNumber, String description,
                                 BigDecimal fee, String subPaymentCode, String remoteIp) {
        // 创建支付请求对象
        PayRequest req = new PayRequest(TenpayConfig.SECRET_KEY);

        // 设置在沙箱中运行，正式环境请设置为false
        req.setInSandBox(TenpayConfig.IN_SANDBOX);
        // 设置财付通App-id: 财付通App注册时，由财付通分配
        req.setAppid(TenpayConfig.APP_ID);
        // *************************以下业务参数名称参考开放平台sdk文档-JAVA****************************
        // 设置用户客户端ip:用户IP，指用户浏览器端IP，不是财付通APP服务器IP
        req.setParameter("spbill_create_ip", remoteIp);


        // 设置商户系统订单号：财付通APP系统内部的订单号,32个字符内、可包含字母,确保在财付通APP系统唯一
        req.setParameter("out_trade_no", orderNumber);
        // 设置商品名称:商品描述，会显示在财付通支付页面上
        req.setParameter("body", description);
        // 设置通知url：接收财付通后台通知的URL，用户在财付通完成支付后，财付通会回调此URL，向财付通APP反馈支付结果。
        // 此URL可能会被多次回调，请正确处理，避免业务逻辑被多次触发。需给绝对路径，例如：http://wap.isv.com/notify.asp
        req.setParameter("notify_url", TenpayConfig.NOTIFY_URL);
        // 设置返回url：用户完成支付后跳转的URL，财付通APP应在此页面上给出提示信息，引导用户完成支付后的操作。
        // 财付通APP不应在此页面内做发货等业务操作，避免用户反复刷新页面导致多次触发业务逻辑造成不必要的损失。
        // 需给绝对路径，例如：http://wap.isv.com/after_pay.asp，通过该路径直接将支付结果以Get的方式返回
        req.setParameter("return_url", TenpayConfig.RETURN_URL);
        // 设置订单总金额，单位为分
        req.setParameter("total_fee", fee.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_DOWN).toString());

        return "<script language=\"javascript\">\r\n" +
                "window.location.href='" + req.getURL() + "';\r\n" +
                "</script>";
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

        // Constants.TEST_SECRET_KEY 签名密钥: 开发者注册时，由财付通分配
        // 创建支付结果反馈响应对象：支付跳转接口为异步返回，用户在财付通完成支付后，财付通通过回调return_url和notify_url向财付通APP反馈支付结果。
        TenpayResponse res = new TenpayResponse(params, TenpayConfig.SECRET_KEY);
        // 获取通知id:支付结果通知id，支付成功返回通知id，要获取订单详细情况需用此ID调用通知验证接口。
        String notifyid = res.getNotifyId();
        // 初始化通知验证请求:财付通APP接收到财付通的支付成功通知后，通过此接口查询订单的详细情况，以确保通知是从财付通发起的，没有被篡改过。
        NotifyQueryRequest req = new NotifyQueryRequest( TenpayConfig.SECRET_KEY);
        // 是否在沙箱运行
        req.setInSandBox(TenpayConfig.IN_SANDBOX);
        // 设置财付通App-id: 财付通App注册时，由财付通分配
        req.setAppid(TenpayConfig.APP_ID);
        req.setParameter("notify_id", notifyid);
        NotifyQueryResponse notifyQueryRes = req.send();

        if (notifyQueryRes.isPayed()) {// 已经支付则更新数据库状态
            result.put(VERIFY_RESULT, VERIFY_RESULT_OK);
            // 获取支付的订单号
            String outTradeNo = notifyQueryRes.getParameter("out_trade_no");
            result.put(ORDER_NUMBER, outTradeNo);
            // ！！！此处强烈建议校验支付金额是否和订单金额一致！！！
            String totalFee = notifyQueryRes.getParameter("total_fee");
            BigDecimal fee = new BigDecimal(totalFee);
            result.put(TOTAL_FEE, fee.divide(new BigDecimal("100"), 2, RoundingMode.HALF_DOWN).toString());
            result.put(SUCCESS_INFO, "success");
        }else {
            result.put(VERIFY_RESULT, VERIFY_RESULT_ERROR);
            result.put(SUCCESS_INFO, "failed");
        }

        return result;
    }
}
