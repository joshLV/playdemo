package controllers;

import java.util.Map;

import models.order.Order;
import models.payment.PaymentFlow;
import models.payment.PaymentJournal;
import models.payment.PaymentUtil;
import play.mvc.Controller;

public class PaymentNotify extends Controller {

    public static void notify(String shihui_partner){
        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow(shihui_partner);
        Map<String, String> result = paymentFlow.notify(PaymentUtil.filterPlayParameter(params.all()));

        boolean success = false;
        String orderNumber = result.get(PaymentFlow.ORDER_NUMBER);
        String fee         = result.get(PaymentFlow.TOTAL_FEE);
        
        if(PaymentFlow.VERIFY_RESULT_OK.equals(result.get(PaymentFlow.VERIFY_RESULT))){
            success = Order.verifyAndPay(orderNumber, fee);
        }

        PaymentJournal.saveNotifyJournal(orderNumber, params.all(), result, success);
        if(success){
            renderText(result.get(PaymentFlow.SUCCESS_INFO));
        }else {
            renderText("failed");
        }
    }
}
