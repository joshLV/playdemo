package controllers;

import java.io.IOException;
import java.util.Map;

import models.payment.AliPaymentFlow;
import models.payment.BillPaymentFlow;
import models.payment.PaymentFlow;
import models.payment.TenpayPaymentFlow;
import models.payment.tenpay.ResponseHandler;
import play.Play;
import play.mvc.Controller;

public class PamentNotify extends Controller {
	private static PaymentFlow paymentFlow = new AliPaymentFlow();
	private static TenpayPaymentFlow tenpayPaymentFlow = new TenpayPaymentFlow();
	private static BillPaymentFlow billPaymentFlow = new BillPaymentFlow();

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
			renderText(handler.doShow(tenpayPaymentFlow.paymentNotify(params.all())));
		} catch (IOException e) {
			renderText("failed");
		}
	}

	/**
	 * 快钱
	 */
	public static void kuaiqianNotify(){
		Map<String,String> map = billPaymentFlow.paymentNotify(params.all());
		if(map != null && !"0".equals(map.get("rtnOK"))){
			renderText("<result>"+map.get("rtnOK")+"</result><redirecturl>"+map.get("rtnUrl")+"</redirecturl>");
		}else {
			renderText("failed");
		}
	}
}
