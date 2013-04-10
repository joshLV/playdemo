package models.payment.test;

import models.payment.PaymentFlow;
import models.payment.PaymentUtil;
import play.Play;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-8-28
 */
public class TestPaymentFlow extends PaymentFlow{
    public static final boolean ON = Play.configuration.getProperty("local.payment", "off").equals("on");
    public static final String PAY_GATE_URL = Play.configuration.getProperty("local.payment.gateway_url", "http://192.168.18.222:1234");
    public static final String RETURN_URL = Play.configuration.getProperty("local.payment.return_url", "http://localhost:9001/order_result/test");
    public static final String NOTIFY_URL = Play.configuration.getProperty("local.payment.notify_url", "");

    @Override
    public String getRequestForm(String orderNumber, String description, BigDecimal fee, String subPaymentCode,
                                 String remoteIp, String ext) {
        Map<String, String> params = new HashMap<>();
        params.put("order_no", orderNumber);
        params.put("desc", description);
        params.put("fee", fee.toString());
        params.put("return_url", RETURN_URL);
        params.put("notify_url", NOTIFY_URL);

        StringBuilder sbHtml = new StringBuilder();
        sbHtml.append("<form id=\"testPay\" name=\"tenPay\" action=\"" + PAY_GATE_URL + "\" method=\"get\" >");

        for(Map.Entry<String, String> entry : params.entrySet()){
            sbHtml.append("<input type=\"hidden\" name=\"")
                    .append(entry.getKey())
                    .append("\" value=\"")
                    .append(entry.getValue())
                    .append("\"/>");
        }

        sbHtml.append("</form><script>document.forms['testPay'].submit();</script>");

        return sbHtml.toString();
    }

    @Override
    public Map<String, String> notify(Map<String, String[]> requestParams) {
        return urlReturn(requestParams);
    }

    @Override
    public Map<String, String> urlReturn(Map<String, String[]> requestParams) {
        Map<String, String> result = new HashMap<>();
        Map<String, String> params = parseRequestParams(requestParams);
        result.put(VERIFY_RESULT, VERIFY_RESULT_OK);
        result.put(ORDER_NUMBER, params.get("order_no"));
        result.put(TOTAL_FEE, params.get("fee"));
        result.put(SUCCESS_INFO, "success");
        result.put(PAYMENT_CODE, PaymentUtil.PARTNER_CODE_TESTPAY);
        return result;
    }
}
