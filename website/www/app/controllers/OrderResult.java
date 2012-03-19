package controllers;

import models.order.*;

import models.payment.AliPaymentFlow;
import models.payment.PaymentFlow;
import play.mvc.Controller;
import java.math.BigDecimal;
import thirdpart.alipay.util.AlipayNotify;

import java.util.*;

public class OrderResult extends Controller {
	private static PaymentFlow paymentFlow = new AliPaymentFlow();
	
    public static void alipayReturn() {

        //验证通知结果
        String errorMessage = null;
        if (!paymentFlow.paymentNotify(params.all())){
            errorMessage = "对不起，暂时无法读取信息，请您稍后再试";
        }
    
        renderTemplate("OrderResult/index.html", errorMessage);

    }

}
