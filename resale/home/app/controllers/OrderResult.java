package controllers;

import models.order.Order;
import models.payment.PaymentUtil;
import models.payment.alipay.AliPaymentFlow;
import org.apache.commons.lang.StringUtils;

import models.payment.PaymentFlow;
import play.mvc.Controller;

import java.util.Map;

public class OrderResult extends Controller {
    private static PaymentFlow paymentFlow = new AliPaymentFlow();

    public static void urlReturn(String partner) {

        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow(partner);
        Map<String, String> result = paymentFlow.urlReturn(PaymentUtil.filterPlayParameter(params.all()));
        String  errorMessage = "对不起，暂时无法读取信息，请您稍后再试";

        String orderNumber = result.get(PaymentFlow.ORDER_NUMBER);
        if(PaymentFlow.VERIFY_RESULT_OK.equals(result.get(PaymentFlow.VERIFY_RESULT))){
            String fee         = result.get(PaymentFlow.TOTAL_FEE);
            boolean processOrderResult = Order.verifyAndPay(orderNumber, fee);

            if(processOrderResult){
                errorMessage = null;
            }
        }
        renderTemplate("OrderResult/index.html", errorMessage, orderNumber);
    }
}
