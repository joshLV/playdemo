package controllers;

import models.payment.AliPaymentFlow;
import models.payment.PaymentFlow;
import play.mvc.Controller;

public class AliPay extends Controller {
    private static PaymentFlow paymentFlow = new AliPaymentFlow();

    public static void paymentNotify(){
        if(paymentFlow.paymentNotify(params.all())){
            renderText("success");
        }else {
            renderText("failed");
        }
   }
}
