package models.payment;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-3-16
 */
public abstract class PaymentFlow {

    public static final String VERIFY_RESULT        = "verify_result";
    public static final String VERIFY_RESULT_OK     = "ok";
    public static final String VERIFY_RESULT_ERROR  = "error";
    public static final String ORDER_NUMBER         = "order_number";
    public static final String TOTAL_FEE            = "total_fee";
    public static final String SUCCESS_INFO         = "success_info";
    public static final String PAYMENT_CODE         = "payment_code";


    /**
     * 返回跳转信息.
     *
     * @param orderNumber 订单号
     * @param description 订单描述
     * @param fee 订单金额
     * @param subPaymentCode 在此第三方支付系统中,所选的支付信息(目前只对快钱有用)
     * @param remoteIp  客户IP
     * @return 跳转信息
     */
    public abstract String getRequestForm(String orderNumber, String description, BigDecimal fee,
                                          String subPaymentCode, String remoteIp, String ext);  //生成form表单

    /**
     * 验证支付后台调用返回参数是否合法.
     * 同时返回四个String参数:
     *   VERIFY_RESULT   是否验证成功, true/false
     *   ORDER_NUMBER    订单号
     *   TOTAL_FEE       支付金额
     *   PAYMENT_CODE    支付方式的代码，对应payment_source表的code字段
     *   SUCCESS_INFO    告知第三方支付服务器的返回内容
     *
     * @param requestParams 请求 参数
     * @return 支付返回参数是否合法等信息
     */
    public abstract Map<String, String> notify(Map<String, String[]> requestParams);

    /**
     * 验证前台页面跳转支付参数是否合法
     * 同时返回三个String参数:
     *   VERIFY_RESULT   是否验证成功, true/false
     *   ORDER_NUMBER    订单号
     *   TOTAL_FEE       支付金额
     *   PAYMENT_CODE    支付方式的代码，对应payment_source表的code字段
     *
     * @param requestParams 请求 参数
     * @return 支付返回参数是否合法等信息
     */
    public abstract Map<String, String> urlReturn(Map<String, String[]> requestParams);

    /**
     * 解析请求参数
     * 如果一个key中对应多个值,仅取第一个,
     * 若需要其他格式,请覆盖此方法(如AliPaymenFlow)
     *
     * @param params 请求参数
     * @return 解析后的格式
     */
    public Map<String, String> parseRequestParams(Map<String, String[]> params){
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String[] value = entry.getValue();
            if (value != null && value.length > 0) {
                result.put(entry.getKey(), value[0]);
            }
        }
        return result;
    }
}
