package controllers;

import java.util.Map;

import models.order.Order;
import models.payment.PaymentFlow;
import models.payment.PaymentUtil;
import play.mvc.Controller;

public class PaymentNotify extends Controller {

    public static void notify(String shihui_partner){
        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow(shihui_partner);
        Map<String, String> result = paymentFlow.notify(PaymentUtil.filterPlayParameter(params.all()));

        if(PaymentFlow.VERIFY_RESULT_OK.equals(result.get(PaymentFlow.VERIFY_RESULT))){
            String orderNumber = result.get(PaymentFlow.ORDER_NUMBER);
            String fee         = result.get(PaymentFlow.TOTAL_FEE);
            boolean processOrderResult = Order.verifyAndPay(orderNumber, fee);

            if(processOrderResult){
                renderText(result.get(PaymentFlow.SUCCESS_INFO));
            }else {
                renderText("failed");
            }

        }else {
            renderText("failed");
        }
    }
}
