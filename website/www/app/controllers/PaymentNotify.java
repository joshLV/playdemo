package controllers;

import java.util.Map;

import com.google.gson.Gson;
import models.order.Order;
import models.payment.PaymentFlow;
import models.payment.PaymentJournal;
import models.payment.PaymentUtil;
import play.Logger;
import play.mvc.Controller;

public class PaymentNotify extends Controller {

    public static void notify(String shihui_partner){
        Logger.info(new Gson().toJson(params.allSimple()));
        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow(shihui_partner);
        if(paymentFlow == null) {
            error("payment partner not found: " + shihui_partner);
            return;
        }
        Map<String, String> result = paymentFlow.notify(PaymentUtil.filterPlayParameter(params.all()));

        boolean success = false;
        String orderNumber = result.get(PaymentFlow.ORDER_NUMBER);
        String fee         = result.get(PaymentFlow.TOTAL_FEE);
        
        if(PaymentFlow.VERIFY_RESULT_OK.equals(result.get(PaymentFlow.VERIFY_RESULT))){
            success = Order.verifyAndPay(orderNumber, fee, result.get(PaymentFlow.PAYMENT_CODE));
        }

        PaymentJournal.saveNotifyJournal(orderNumber, params.all(), result, success);
        if(success){
            renderText(result.get(PaymentFlow.SUCCESS_INFO));
        }else {
            renderText("failed");
        }
    }
}
