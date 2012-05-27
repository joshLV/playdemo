package controllers;

import controllers.modules.resale.cas.SecureCAS;
import models.order.Order;
import models.payment.PaymentJournal;
import models.payment.PaymentUtil;

import models.payment.PaymentFlow;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Map;

@With(SecureCAS.class)
public class OrderResult extends Controller {

    /**
     * 支付 url 跳转回来.
     *
     * @param shihui_partner 第三方支付代码
     */
    public static void urlReturn(String shihui_partner) {

        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow(shihui_partner);
        Map<String, String> result = paymentFlow.urlReturn(PaymentUtil.filterPlayParameter(params.all()));
        String  errorMessage = "对不起，暂时无法读取信息，请您稍后再试";

        String orderNumber = result.get(PaymentFlow.ORDER_NUMBER);
        String fee         = result.get(PaymentFlow.TOTAL_FEE);
        boolean success = false;
        if(PaymentFlow.VERIFY_RESULT_OK.equals(result.get(PaymentFlow.VERIFY_RESULT))){
            boolean processOrderResult = Order.verifyAndPay(orderNumber, fee);

            if(processOrderResult){
                errorMessage = null;
                success = true;
            }
        }
        PaymentJournal.saveUrlReturnJournal(orderNumber, params.all(), result, success);
        renderTemplate("OrderResult/index.html", errorMessage, orderNumber);
    }
}
