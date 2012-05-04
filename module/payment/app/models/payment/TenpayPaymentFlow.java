package models.payment;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Map;

import models.accounts.PaymentCallbackLog;
import models.accounts.TradeBill;
import models.accounts.util.TradeUtil;
import models.order.Order;
import models.order.OrderStatus;
import models.payment.tenpay.PayRequestHandler;
import models.payment.tenpay.util.MD5Util;
import models.payment.tenpay.util.TenpayUtil;
import play.Logger;
import play.Play;
import play.mvc.Http.Request;

/**
 * 财付通支付接口
 * 
 * @author yanjy
 *
 */
public class TenpayPaymentFlow {


	/**
	 * 产生表单数据
	 * @param order
	 * @return 
	 * @throws UnsupportedEncodingException
	 */
	public String generatetTenpayForm(Order order) throws UnsupportedEncodingException{
		//必填参数//

		//请与贵网站订单系统中的唯一订单号匹配
		String out_trade_no = order.orderNumber;
		//订单名称，显示在支付宝收银台里的“商品名称”里，显示在支付宝的交易管理的
		//“商品名称”的列表里。
		String subject = null;
		if(order.orderItems.size() == 1){
			subject = order.orderItems.get(0).goodsName ;
		}else if(order.orderItems.size() > 1){
			subject = order.orderItems.get(0).goodsName + "等商品";
		}
		//订单总金额，显示在支付宝收银台里的“应付总额”里
		String total_fee = order.needPay.toString();

		return generateFormBase(out_trade_no, subject, total_fee);
	} 

	public String generateFormBase(String trade_no, String subject, String total_fee) throws UnsupportedEncodingException{
		//商户号
		String bargainor_id = "1211869101";

		//密钥
		String key = Play.configuration.getProperty("tenpay.key","cdaae5034d86383038eddcd1b8834c89");

		//回调通知URL，成功的时候处理订单信息
		String return_url = Play.configuration.getProperty("tenpay.notify_url","http://192.168.18.140:29001/pay/tenpay_notify");
		
		//当前时间 yyyyMMddHHmmss
		String currTime = TenpayUtil.getCurrTime();

		//8位日期
		String strDate = currTime.substring(0, 8);

		//6位时间
		String strTime = currTime.substring(8, currTime.length());

		//四位随机数
		String strRandom = TenpayUtil.buildRandom(4) + "";

		//10位序列号,可以自行调整。
		String strReq = strTime + strRandom;

		//商家订单号,长度若超过32位，取前32位。财付通只记录商家订单号，不保证唯一。
		String sp_billno = trade_no;

		//财付通交易单号，规则为：10位商户号+8位时间（YYYYmmdd)+10位流水号
		String transaction_id = bargainor_id + strDate + strReq;
		//订单总金额，显示在支付宝收银台里的“应付总额”里
		BigDecimal amount= 	new BigDecimal(total_fee).multiply(new BigDecimal(100)).divide(new BigDecimal(1),0,BigDecimal.ROUND_HALF_UP);
		//创建PayRequestHandler实例
		PayRequestHandler reqHandler = new PayRequestHandler();

		//设置密钥
		reqHandler.setKey(key);

		//初始化
		reqHandler.init();

		//-----------------------------
		//设置支付参数
		//-----------------------------
		reqHandler.setParameter("bargainor_id", bargainor_id);			//商户号
		reqHandler.setParameter("sp_billno", sp_billno);				//商家订单号
		reqHandler.setParameter("transaction_id", transaction_id);		//财付通交易单号
		reqHandler.setParameter("return_url", return_url);				//支付通知url
		reqHandler.setParameter("desc", "商品名称：" + subject);	//商品名称
		reqHandler.setParameter("total_fee", String.valueOf(amount));				//商品金额,以分为单位

		//用户ip,测试环境时不要加这个ip参数，正式环境再加此参数
		reqHandler.setParameter("spbill_create_ip",Request.current().remoteAddress);

		//获取请求带参数的url
		//String requestUrl = reqHandler.getRequestURL();

		//获取debug信息
		//		String debuginfo = reqHandler.getDebugInfo();

		return reqHandler.getRequestURL();      
	}


	/**
	 * 是否财付通签名
	 * @Override
	 * @return boolean
	 */
	public boolean isTenpaySign(Map<String, String[]> params) {
		String enc = "gbk";
		//获取参数
		String cmdno = params.get("cmdno") == null ? null : params.get("cmdno")[0];
		String pay_result = params.get("pay_result") == null ? null : params.get("pay_result")[0];
		String date = params.get("date") == null ? null : params.get("date")[0];
		String transaction_id = params.get("transaction_id") == null ? null : params.get("transaction_id")[0];
		String sp_billno  = params.get("sp_billno") == null ? null : params.get("sp_billno")[0];
		String total_fee    = params.get("total_fee") == null ? null : params.get("total_fee")[0];


		String fee_type = params.get("fee_type") == null ? null : params.get("fee_type")[0];;
		String attach = params.get("attach") == null ? null : params.get("attach")[0];;
		//密钥
		String key = Play.configuration.getProperty("tenpay.key","cdaae5034d86383038eddcd1b8834c89");
		String tenpaySign = params.get("sign") == null ? "" : params.get("sign")[0];

		//组织签名串
		StringBuffer sb = new StringBuffer();
		sb.append("cmdno=" + cmdno + "&");
		sb.append("pay_result=" + pay_result + "&");
		sb.append("date=" + date + "&");
		sb.append("transaction_id=" + transaction_id + "&");
		sb.append("sp_billno=" + sp_billno + "&");
		sb.append("total_fee=" + total_fee + "&");
		sb.append("fee_type=" + fee_type + "&");
		sb.append("attach=" + attach + "&");
		sb.append("key=" + key);

		//算出摘要
		String sign = MD5Util.MD5Encode(sb.toString(), enc).toUpperCase();

		return tenpaySign.equals(sign);
	} 

	/**
	 * 回调接口，处理订单信息
	 * 
	 * @param params 参数
	 * @return 处理成功或失败
	 */
	public boolean paymentNotify(Map<String, String[]> params) {

		String transaction_id = params.get("transaction_id") == null ? null : params.get("transaction_id")[0];
		String orderNo  = params.get("sp_billno") == null ? null : params.get("sp_billno")[0];
		BigDecimal orderAmount    = params.get("total_fee") == null ? null : new BigDecimal(params.get("total_fee")[0]);
		String subject = params.get("subject") == null ? null : params.get("subject")[0];

		boolean success = true;
		//判断签名
		if(isTenpaySign(params)) {

			//支付结果
			String pay_result = params.get("pay_result") == null ? null : params.get("pay_result")[0];

			String log = "tenpay_notify:" +
					"交易状态:" + pay_result + "," +
					"交易号:" + transaction_id + "," +
					"订单号:" + orderNo + "," +
					"总金额:" + orderAmount + "," +
					"商品名称:" + subject;
			Logger.info(log);
			PaymentCallbackLog callbackLog =
					new PaymentCallbackLog("afasfa", "tenpay", orderNo,orderAmount, pay_result, log);

			if( "0".equals(pay_result) ) {
				Order order = Order.find("byOrderNumber",orderNo).first();

				if (order == null || order.orderNumber == null ) {
					Logger.error("tenpay_notify:没有此订单信息！");
					callbackLog.status = "invalid_trade";
					success = false;
				}

				BigDecimal amount= 	order.amount.multiply(new BigDecimal(100)).divide(new BigDecimal(1),0,BigDecimal.ROUND_HALF_UP);

				//订单支付金额不相符
				if(amount.compareTo(orderAmount) !=0) {
					Logger.error("tenpay_notify:订单支付金额不相符！");
					callbackLog.status = "invalid_order_money";
					success = false;
				}

				if (OrderStatus.PAID.equals(order.status)) {
					Logger.error("tenpay_notify:订单已被处理:" + orderNo);
					callbackLog.status = "processed";
				} else if (success){
					Long tradeId = order.payRequestId;
					TradeBill tradeBill = TradeBill.findById(tradeId);
					if(tradeBill != null){
						//最终所有条件满足
						TradeUtil.success(tradeBill);
						order.paid();
					} else {
						callbackLog.status = "no_trade_found";
						success = false;
					}
				}

			} else {
				Logger.error("tenpay_notify:支付失败");
				callbackLog.status = "tenpay_verify_failed";
				success = false;
			}
			callbackLog.save();
		} else {
			Logger.error("tenpay_notify:认证签名失败");
			success = false;
		}

		return success;
	}
}
