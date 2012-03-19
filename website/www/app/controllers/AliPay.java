package controllers;

import models.accounts.PaymentCallbackLog;
import models.accounts.TradeBill;
import models.accounts.util.TradeUtil;
import models.order.Order;
import models.order.OrderStatus;

import models.payment.AliPaymentFlow;
import models.payment.PaymentFlow;
import play.Logger;
import play.mvc.Controller;
import thirdpart.alipay.util.AlipayNotify;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class AliPay extends Controller {
    private static PaymentFlow paymentFlow = new AliPaymentFlow();

    public static void paymentNotify(){
        paymentFlow.paymentNotify(params.all());
   }
}
