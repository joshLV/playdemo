package models.payment.kuaiqian;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import models.payment.PaymentFlow;
import models.payment.PaymentUtil;
import org.apache.commons.lang.StringUtils;
import play.Logger;

public class KuaiqianPaymentFlow extends PaymentFlow {
    private static final String DATE_FORMAT = "yyyyMMddHHmmss";

    /**
     * 产生表单数据
     *
     * @param orderNumber    订单编号
     * @param description    订单描述
     * @param fee            订单金额
     * @param subPaymentCode 该支付方式中所对应的银行代码
     * @return 表单
     */
    @Override
    public String getRequestForm(String orderNumber, String description, BigDecimal fee, String subPaymentCode,
                                 String remoteIp, String ext) {
        String orderAmount = fee.multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP).toString();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        //参数需要按照顺序添加
        Map<String, String> params = new LinkedHashMap<>();

        params.put("inputCharset", "1"); //编码方式，1代表 UTF-8; 2 代表 GBK; 3代表 GB2312 默认为1,该参数必填。
        params.put("pageUrl", ""); //接收支付结果的页面地址，该参数一般置为空即可。
        params.put("bgUrl", KuaiqianConfig.NOTIFY_URL); //服务器接收支付结果的后台地址，该参数务必填写，不能为空。
        params.put("version", "v2.0"); //网关版本，固定值：v2.0,该参数必填。
        params.put("language", "1"); //语言种类，1代表中文显示，2代表英文显示。默认为1,该参数必填。
        params.put("signType", "4"); //签名类型,该值为4，代表PKI加密方式,该参数必填。
        params.put("merchantAcctId", KuaiqianConfig.APP_ID); //人民币网关账号，该账号为11位人民币网关商户编号+01,该参数必填。
        params.put("payerName", ""); //支付人姓名,可以为空。
        params.put("payerContactType", "1"); //支付人联系类型，1 代表电子邮件方式；2 代表手机联系方式。可以为空。
        params.put("payerContact", ""); //支付人联系方式，与payerContactType设置对应，payerContactType为1，则填写邮箱地址；payerContactType为2，则填写手机号码。可以为空。
        params.put("orderId", orderNumber); //请与贵网站订单系统中的唯一订单号匹配
        params.put("orderAmount", orderAmount); //订单总金额 以分为单位
        params.put("orderTime", dateFormat.format(new Date())); //订单提交时间 14位数字。年[4位]月[2位]日[2位]时[2位]分[2位]秒[2位] 如；20080101010101
        params.put("productName", description); //商品名称，可以为空。
        params.put("productNum", ""); //商品数量，可以为空。
        params.put("productId", ""); //商品代码，可以为空。
        params.put("productDesc", ""); //商品描述，可以为空。
        params.put("ext1", ""); //扩展字段1，商户可以传递自己需要的参数，支付完快钱会原值返回，可以为空。
        params.put("ext2", ""); //扩展自段2，商户可以传递自己需要的参数，支付完快钱会原值返回，可以为空。
        params.put("payType", "10"); //支付方式，一般为00，代表所有的支付方式。如果是银行直连商户，该值为10，必填。
        params.put("bankId", subPaymentCode); //银行代码
        params.put("redoFlag", "0"); //同一订单禁止重复提交标志，实物购物车填1，虚拟产品用0。1代表只能提交一次，0代表在支付不成功情况下可以再提交。可为空。
        params.put("pid", ""); //快钱合作伙伴的帐户号，即商户编号，可为空。


        // signMsg 签名字符串 不可空，生成加密签名串
        String signMsg = KuaiqianPkiPair.signMsg(join(params));
        params.put("signMsg", signMsg);

        StringBuilder sbHtml = new StringBuilder();
        sbHtml.append("<form id=\"kqPay\" name=\"kqPay\" action=\"")
                .append(KuaiqianConfig.SERVER_URL)
                .append("\" method=\"post\" >");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sbHtml.append("<input type=\"hidden\" name=\"")
                    .append(entry.getKey())
                    .append("\" value=\"")
                    .append(entry.getValue())
                    .append("\"/>");
        }
        sbHtml.append("</form><script>document.forms['kqPay'].submit();</script>");

        return sbHtml.toString();
    }

    /**
     * 回调接口，处理订单信息
     *
     * @param requestParams 请求
     * @return 处理成功或失败
     */
    @Override
    public Map<String, String> notify(Map<String, String[]> requestParams) {
        Map<String, String> result = new HashMap<>();
        Map<String, String> params = parseRequestParams(requestParams);
        Map<String, String> verifyParams = new LinkedHashMap<>();

        verifyParams.put("merchantAcctId", params.get("merchantAcctId")); //获取人民币网关账户号
        verifyParams.put("version", params.get("version")); //获取网关版本.固定值 快钱会根据版本号来调用对应的接口处理程序。 本代码版本号固定为v2.0
        verifyParams.put("language", params.get("language")); //获取语言种类.固定选择值。只能选择1、2、3 1代表中文；2代表英文 默认值为1
        verifyParams.put("signType", params.get("signType")); //签名类型.固定值 1代表MD5签名 当前版本固定为1
        //获取支付方式 值为：10、11、12、13、14
        // 00：组合支付（网关支付页面显示快钱支持的各种支付方式，推荐使用）
        // 10：银行卡支付（网关支付页面只显示银行卡支付）
        // 11：电话银行支付（网关支付页面只显示电话支付）.
        // 12：快钱账户支付（网关支付页面只显示快钱账户支付）.
        // 13：线下支付（网关支付页面只显示线下支付方式）.
        // 14：B2B支付（网关支付页面只显示B2B支付，但需要向快钱申请开通才能使用）
        verifyParams.put("payType", params.get("payType"));
        verifyParams.put("bankId", params.get("bankId")); //获取银行代码 参见银行代码列表
        verifyParams.put("orderId", params.get("orderId")); //获取商户订单号
        verifyParams.put("orderTime", params.get("orderTime")); //获取订单提交时间 ///获取商户提交订单时的时间.14位数字。年[4位]月[2位]日[2位]时[2位]分[2位]秒[2位] ///如：20080101010101
        verifyParams.put("orderAmount", params.get("orderAmount")); //获取原始订单金额 订单提交到快钱时的金额，单位为分。 比方2 ，代表0.02元
        verifyParams.put("dealId", params.get("dealId")); //获取快钱交易号 获取该交易在快钱的交易号
        verifyParams.put("bankDealId", params.get("bankDealId")); //获取银行交易号 如果使用银行卡支付时，在银行的交易号。如不是通过银行支付，则为空
        verifyParams.put("dealTime", params.get("dealTime")); //获取在快钱交易时间 14位数字。年[4位]月[2位]日[2位]时[2位]分[2位]秒[2位] 如；20080101010101
        verifyParams.put("payAmount", params.get("payAmount")); //获取实际支付金额 单位为分 比方 2 ，代表0.02元
        verifyParams.put("fee", params.get("fee")); //获取交易手续费 单位为分 比方 2 ，代表0.02元
        verifyParams.put("ext1", params.get("ext1")); //获取扩展字段1
        verifyParams.put("ext2", params.get("ext2")); //获取扩展字段2
        verifyParams.put("payResult", params.get("payResult")); //获取处理结果 10代表 成功11代表 失败 00代表 下订单成功（仅对电话银行支付订单返回）;01代表 下订单失败（仅对电话银行支付订单返回）
        verifyParams.put("errCode", params.get("errCode")); //获取错误代码 详细见文档错误代码列表


        boolean flag = KuaiqianPkiPair.enCodeByCer(join(verifyParams), params.get("signMsg")); //获取加密签名串,并与本地计算结果进行比较
        int rtnOK = 0;
        String rtnUrl = KuaiqianConfig.RETURN_URL;

        result.put(VERIFY_RESULT, VERIFY_RESULT_ERROR);
        result.put(ORDER_NUMBER, params.get("orderId"));
        BigDecimal payAmount = new BigDecimal(params.get("payAmount"));
        payAmount = payAmount.divide(new BigDecimal("100"), 2, RoundingMode.HALF_DOWN);
        result.put(TOTAL_FEE, payAmount.toString());
        if (flag) {
            switch (Integer.parseInt(params.get("payResult"))) {
                case 10:
                    rtnOK = 1;
                    result.put(PAYMENT_CODE, PaymentUtil.PARTNER_CODE_99BILL + "_" + params.get("bankId").toUpperCase());
                    rtnUrl += String.format("?%s=%s&%s=%s&%s=%s&%s=%s",
                            VERIFY_RESULT, VERIFY_RESULT_OK,
                            ORDER_NUMBER, params.get("orderId"),
                            TOTAL_FEE, payAmount.toString(),
                            PAYMENT_CODE, result.get(PAYMENT_CODE));
                    result.put(VERIFY_RESULT, VERIFY_RESULT_OK);
                    break;
                default:
                    break;
            }
        }
        String successInfo = "<result>" + rtnOK + "</result><redirecturl>" + rtnUrl + "</redirecturl>";
        result.put(SUCCESS_INFO, successInfo);

        Logger.info("kuaiqian callback result: flag:" + flag + ";payResult:" + params.get("payResult"));
        return result;
    }

    @Override
    public Map<String, String> urlReturn(Map<String, String[]> requestParams) {
        return parseRequestParams(requestParams);
    }

    private String join(Map<String, String> params) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String value = entry.getValue();
            if (StringUtils.isNotBlank(value)) {
                result.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append("&");
            }
        }
        if (result.length() > 0) {
            return result.substring(0, result.length() - 1);
        } else {
            return "";
        }
    }
}
