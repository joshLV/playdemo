package controllers;

import java.io.IOException;

import models.payment.AliPaymentFlow;
import models.payment.PaymentFlow;
import models.payment.TenpayPaymentFlow;
import models.payment.tenpay.ResponseHandler;
import play.Play;
import play.mvc.Controller;

public class PamentNotify extends Controller {
	private static PaymentFlow paymentFlow = new AliPaymentFlow();
	private static TenpayPaymentFlow tenpayPaymentFlow = new TenpayPaymentFlow();

	/**
	 * 支付宝
	 */
	public static void aliPayNotify(){
		if(paymentFlow.paymentNotify(params.all())){
			renderText("success");
		}else {
			renderText("failed");
		}
	}

	/**
	 * 财付通
	 */
	public static void tenpayNotify(){
		ResponseHandler handler = new ResponseHandler();
		try {
			String url =Play.configuration.getProperty("tenpay.return_url");
			if(tenpayPaymentFlow.paymentNotify(params.all())){
				renderText(handler.doShow(url));
			}else {
				renderText(handler.doShow(url+"?error=1"));
			}
		} catch (IOException e) {
			renderText("failed");
		}
	}
}
