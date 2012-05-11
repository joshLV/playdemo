package models.payment;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import models.accounts.PaymentCallbackLog;
import models.accounts.TradeBill;
import models.accounts.util.TradeUtil;
import models.order.Order;
import models.order.OrderStatus;
import play.Logger;
import play.Play;

public class BillPaymentFlow {

	public String systemTime(Date date) {
		String fromFormat = "yyyyMMddHHmmss";
		SimpleDateFormat format = new SimpleDateFormat(fromFormat,
				Locale.SIMPLIFIED_CHINESE);
		TimeZone zone = TimeZone.getTimeZone("GMT+8");
		format.setTimeZone(zone);

		return format.format(date);
	}

	/**
	 * 产生表单数据
	 * @param order
	 * @return 
	 * @throws UnsupportedEncodingException
	 */
	public  String generateForm(Order order){
		//必填参数//

		//请与贵网站订单系统中的唯一订单号匹配
		String orderId = order.orderNumber;
		//订单名称，显示在支付宝收银台里的“商品名称”里，显示在支付宝的交易管理的
		//“商品名称”的列表里。
		String subject = null;
		if(order.orderItems.size() == 1){
			subject = order.orderItems.get(0).goodsName ;
		}else if(order.orderItems.size() > 1){
			subject = order.orderItems.get(0).goodsName + "等商品";
		}
		//		//订单总金额，显示在支付宝收银台里的“应付总额”里
		String orderAmount= String.valueOf(order.amount.multiply(new BigDecimal(100)).divide(new BigDecimal(1),0,BigDecimal.ROUND_HALF_UP));
		//
		String bankId=order.payMethod;
		//订单提交时间divide(1,0,BigDecimal.ROUND_HALF_UP))
		///14位数字。年[4位]月[2位]日[2位]时[2位]分[2位]秒[2位]
		///如；20080101010101
		String orderTime=systemTime(order.createdAt);

		//人民币网关账号，该账号为11位人民币网关商户编号+01,该参数必填。
		String merchantAcctId = Play.configuration.getProperty("99bill.merchantAcctId","1002034040901");
		//编码方式，1代表 UTF-8; 2 代表 GBK; 3代表 GB2312 默认为1,该参数必填。
		String inputCharset = "1";
		//接收支付结果的页面地址，该参数一般置为空即可。
		String pageUrl = "";
		//服务器接收支付结果的后台地址，该参数务必填写，不能为空。
		String bgUrl = Play.configuration.getProperty("99bill.notify_url","http://test.uhuila.com:29001/pay/99bill_notify");
		//网关版本，固定值：v2.0,该参数必填。
		String version =  "v2.0";
		//语言种类，1代表中文显示，2代表英文显示。默认为1,该参数必填。
		String language =  "1";
		//签名类型,该值为4，代表PKI加密方式,该参数必填。
		String signType =  "4";
		//支付人姓名,可以为空。
		String payerName= ""; 
		//支付人联系类型，1 代表电子邮件方式；2 代表手机联系方式。可以为空。
		String payerContactType =  "1";
		//支付人联系方式，与payerContactType设置对应，payerContactType为1，则填写邮箱地址；payerContactType为2，则填写手机号码。可以为空。
		String payerContact =  "";
		//商品名称，可以为空。
		String productName= subject; 
		//商品数量，可以为空。
		String productNum = "";
		//商品代码，可以为空。
		String productId = "";
		//商品描述，可以为空。
		String productDesc = "";
		//扩展字段1，商户可以传递自己需要的参数，支付完快钱会原值返回，可以为空。
		String ext1 = "";
		//扩展自段2，商户可以传递自己需要的参数，支付完快钱会原值返回，可以为空。
		String ext2 = "";
		//支付方式，一般为00，代表所有的支付方式。如果是银行直连商户，该值为10，必填。
		String payType = "10";
		//银行代码，如果payType为00，该值可以为空；如果payType为10，该值必须填写，具体请参考银行列表。
		//		String bankId = "";
		//同一订单禁止重复提交标志，实物购物车填1，虚拟产品用0。1代表只能提交一次，0代表在支付不成功情况下可以再提交。可为空。
		String redoFlag = "1";
		//快钱合作伙伴的帐户号，即商户编号，可为空。
		String pid = "";
		// signMsg 签名字符串 不可空，生成加密签名串
		String signMsgVal = "";
		signMsgVal = appendParam(signMsgVal, "inputCharset", inputCharset);
		signMsgVal = appendParam(signMsgVal, "pageUrl", pageUrl);
		signMsgVal = appendParam(signMsgVal, "bgUrl", bgUrl);
		signMsgVal = appendParam(signMsgVal, "version", version);
		signMsgVal = appendParam(signMsgVal, "language", language);
		signMsgVal = appendParam(signMsgVal, "signType", signType);
		signMsgVal = appendParam(signMsgVal, "merchantAcctId",merchantAcctId);
		signMsgVal = appendParam(signMsgVal, "payerName", payerName);
		signMsgVal = appendParam(signMsgVal, "payerContactType",payerContactType);
		signMsgVal = appendParam(signMsgVal, "payerContact", payerContact);
		signMsgVal = appendParam(signMsgVal, "orderId", orderId);
		signMsgVal = appendParam(signMsgVal, "orderAmount", orderAmount);
		signMsgVal = appendParam(signMsgVal, "orderTime", orderTime);
		signMsgVal = appendParam(signMsgVal, "productName", productName);
		signMsgVal = appendParam(signMsgVal, "productNum", productNum);
		signMsgVal = appendParam(signMsgVal, "productId", productId);
		signMsgVal = appendParam(signMsgVal, "productDesc", productDesc);
		signMsgVal = appendParam(signMsgVal, "ext1", ext1);
		signMsgVal = appendParam(signMsgVal, "ext2", ext2);
		signMsgVal = appendParam(signMsgVal, "payType", payType);
		signMsgVal = appendParam(signMsgVal, "bankId", bankId);
		signMsgVal = appendParam(signMsgVal, "redoFlag", redoFlag);
		signMsgVal = appendParam(signMsgVal, "pid", pid);
		KuaiQianPkipair pki = new KuaiQianPkipair();
		String signMsg = pki.signMsg(signMsgVal);

		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("inputCharset", inputCharset);
		sParaTemp.put("pageUrl", pageUrl);
		sParaTemp.put("bgUrl", bgUrl);
		sParaTemp.put("version", version);
		sParaTemp.put("language", language);	
		sParaTemp.put("signType", signType);
		sParaTemp.put("merchantAcctId", merchantAcctId);
		sParaTemp.put("payerName", payerName);
		sParaTemp.put("payerContactType", payerContactType);
		sParaTemp.put("payerContact", payerContact);
		sParaTemp.put("orderId", orderId);	
		sParaTemp.put("orderAmount", orderAmount);
		sParaTemp.put("orderTime", orderTime);
		sParaTemp.put("productName", productName);
		sParaTemp.put("productNum", productNum);
		sParaTemp.put("productId", productId);
		sParaTemp.put("productDesc", productDesc);	
		sParaTemp.put("ext1", ext1);
		sParaTemp.put("payType", payType);
		sParaTemp.put("ext2", ext2);
		sParaTemp.put("bankId", bankId);
		sParaTemp.put("redoFlag", redoFlag);
		sParaTemp.put("pid", pid);
		sParaTemp.put("signMsg", signMsg);
		return buildForm(sParaTemp);      
	}


	/**
	 * 构造提交表单HTML数据
	 * @param sParaTemp 请求参数数组
	 * @return 提交表单HTML文本
	 */
	public static String buildForm(Map<String, String> sParaTemp) {
		List<String> keys = new ArrayList<String>(sParaTemp.keySet());

		StringBuffer sbHtml = new StringBuffer();

		sbHtml.append("<form id=\"kqPay\" name=\"kqPay\" action=\"https://www.99bill.com/gateway/recvMerchantInfoAction.htm\" method=\"post\" >");

		for (int i = 0; i < keys.size(); i++) {
			String name = (String) keys.get(i);
			String value = (String) sParaTemp.get(name);
			sbHtml.append("<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>");
		}

		sbHtml.append("</form><script>document.forms['kqPay'].submit();</script>");

		return sbHtml.toString();
	}

	public String appendParam(String returns, String paramId, String paramValue) {
		if (!"".equals(returns)) {
			if (!"".equals(paramValue)) {
				returns += "&" + paramId + "=" + paramValue;
			}
		} else {
			if (!"".equals(paramValue)) {
				returns = paramId + "=" + paramValue;
			}
		}

		return returns;
	}

	public String doParam(String[] paramValue) {
		String returns = paramValue == null ? "" : paramValue[0].trim();
		return returns;
	}

	/**
	 * 回调接口，处理订单信息
	 * 
	 * @param params 参数
	 * @return 处理成功或失败
	 */
	public Map<String,String> paymentNotify(Map<String, String[]> params) {
		System.out.println("11111111111111111111");
		//获取人民币网关账户号
		String merchantAcctId = doParam(params.get("merchantAcctId"));

		//获取网关版本.固定值
		///快钱会根据版本号来调用对应的接口处理程序。
		///本代码版本号固定为v2.0
		String version = doParam(params.get("version"));

		//获取语言种类.固定选择值。
		///只能选择1、2、3
		///1代表中文；2代表英文
		///默认值为1
		String language = doParam(params.get("language"));

		//签名类型.固定值
		///1代表MD5签名
		///当前版本固定为1
		String signType = doParam(params.get("signType"));

		//获取支付方式
		///值为：10、11、12、13、14
		///00：组合支付（网关支付页面显示快钱支持的各种支付方式，推荐使用）10：银行卡支付（网关支付页面只显示银行卡支付）.11：电话银行支付（网关支付页面只显示电话支付）.12：快钱账户支付（网关支付页面只显示快钱账户支付）.13：线下支付（网关支付页面只显示线下支付方式）.14：B2B支付（网关支付页面只显示B2B支付，但需要向快钱申请开通才能使用）
		String payType = doParam(params.get("payType"));
		//获取银行代码
		///参见银行代码列表
		String bankId = doParam(params.get("bankId"));

		//获取商户订单号
		String orderId = doParam(params.get("orderId"));

		//获取订单提交时间
		///获取商户提交订单时的时间.14位数字。年[4位]月[2位]日[2位]时[2位]分[2位]秒[2位]
		///如：20080101010101
		String orderTime = doParam(params.get("orderTime") );

		//获取原始订单金额
		///订单提交到快钱时的金额，单位为分。
		///比方2 ，代表0.02元
		String orderAmount = doParam(params.get("orderAmount"));

		//获取快钱交易号
		///获取该交易在快钱的交易号
		String dealId = doParam(params.get("dealId"));
		//获取银行交易号
		///如果使用银行卡支付时，在银行的交易号。如不是通过银行支付，则为空
		String bankDealId = doParam(params.get("bankDealId"));

		//获取在快钱交易时间
		///14位数字。年[4位]月[2位]日[2位]时[2位]分[2位]秒[2位]
		///如；20080101010101
		String dealTime = doParam(params.get("dealTime"));

		//获取实际支付金额
		///单位为分
		///比方 2 ，代表0.02元
		String payAmount = doParam(params.get("payAmount"));

		//获取交易手续费
		///单位为分
		///比方 2 ，代表0.02元
		String fee = doParam(params.get("fee"));
		//获取扩展字段1
		String ext1 = doParam(params.get("ext1"));
		//获取扩展字段2
		String ext2 = doParam(params.get("ext2"));
		//获取处理结果
		///10代表 成功11代表 失败
		///00代表 下订单成功（仅对电话银行支付订单返回）;01代表 下订单失败（仅对电话银行支付订单返回）
		String payResult = doParam(params.get("payResult"));
		//获取错误代码
		///详细见文档错误代码列表
		String errCode = doParam(params.get("errCode") );
		//获取加密签名串
		String signMsg = doParam(params.get("signMsg"));
		String merchantSignMsgVal = "";
		merchantSignMsgVal = appendParam(merchantSignMsgVal,"merchantAcctId", merchantAcctId);
		merchantSignMsgVal = appendParam(merchantSignMsgVal, "version",version);
		merchantSignMsgVal = appendParam(merchantSignMsgVal, "language",language);
		merchantSignMsgVal = appendParam(merchantSignMsgVal, "signType",signType);
		merchantSignMsgVal = appendParam(merchantSignMsgVal, "payType",payType);
		merchantSignMsgVal = appendParam(merchantSignMsgVal, "bankId",bankId);
		merchantSignMsgVal = appendParam(merchantSignMsgVal, "orderId",orderId);
		merchantSignMsgVal = appendParam(merchantSignMsgVal, "orderTime",orderTime);
		merchantSignMsgVal = appendParam(merchantSignMsgVal, "orderAmount",orderAmount);
		merchantSignMsgVal = appendParam(merchantSignMsgVal, "dealId",dealId);
		merchantSignMsgVal = appendParam(merchantSignMsgVal, "bankDealId",bankDealId);
		merchantSignMsgVal = appendParam(merchantSignMsgVal, "dealTime",dealTime);
		merchantSignMsgVal = appendParam(merchantSignMsgVal, "payAmount",payAmount);
		merchantSignMsgVal = appendParam(merchantSignMsgVal, "fee", fee);
		merchantSignMsgVal = appendParam(merchantSignMsgVal, "ext1", ext1);
		merchantSignMsgVal = appendParam(merchantSignMsgVal, "ext2", ext2);
		merchantSignMsgVal = appendParam(merchantSignMsgVal, "payResult",payResult);
		merchantSignMsgVal = appendParam(merchantSignMsgVal, "errCode",errCode);
		KuaiQianPkipair pki = new KuaiQianPkipair();
		boolean flag = pki.enCodeByCer(merchantSignMsgVal, signMsg);
		System.out.println("11111111111111111111"+flag);
		int rtnOK =0;
		String rtnUrl = Play.configuration.getProperty("99bill.return_url","http://test.uhuila.com:29001/orders/99billpay_result");
		System.out.println("11111111111111111111"+rtnUrl);
		
		if(flag){
			System.out.println("11111111111111111111payResult"+payResult);
			String log = "99bill_notify:" +
					"交易状态:" + payResult + "," +
					"交易号:" + bankDealId + "," +
					"订单号:" + orderId + "," +
					"总金额:" + orderAmount + "," +
					"商品名称:" + "";
			Logger.info(log);
			PaymentCallbackLog callbackLog =
					new PaymentCallbackLog("", "99bill", orderId,new BigDecimal(orderAmount), payResult, log);

			switch(Integer.parseInt(payResult)){
			case 10:
				Order order = Order.find("byOrderNumber",orderId).first();
				if (order == null || order.orderNumber == null ) {
					Logger.error("99bill_notify:没有此订单信息！");
					callbackLog.status = "invalid_trade";
					rtnUrl+="?rtnOK=-1";
					rtnOK=1;
					break;
				}
				BigDecimal amount= 	order.amount.multiply(new BigDecimal(100)).divide(new BigDecimal(1),0,BigDecimal.ROUND_HALF_UP);

				//订单支付金额不相符
				if(amount.compareTo(new BigDecimal(orderAmount)) !=0) {
					Logger.error("99bill_notify:订单支付金额不相符！");
					callbackLog.status = "invalid_order_money";
					rtnOK=1;
					rtnUrl+="?rtnOK=-2";
					break;
				}

				if (OrderStatus.PAID.equals(order.status)) {
					Logger.error("99bill_notify:订单已被处理:" + orderId);
					callbackLog.status = "processed";
					rtnOK=1;
					break;
				} else if (OrderStatus.UNPAID.equals(order.status)){
					Long tradeId = order.payRequestId;
					TradeBill tradeBill = TradeBill.findById(tradeId);
					//最终所有条件满足
					TradeUtil.success(tradeBill);
					order.paid();
					rtnOK=1;
					break;
				}
				//以下是我们快钱设置的show页面，商户需要自己定义该页面。

			default:
				rtnOK=1;
				//以下是我们快钱设置的show页面，商户需要自己定义该页面。
				rtnUrl+="?rtnOK=-3";
				break;
			}
		} else {
			rtnOK=1;
			rtnUrl+="?rtnOK=-3";
		}	
		System.out.println("rtnUrl====================="+rtnUrl);
		Map<String,String> map = new HashMap();
		map.put("rtnOK", String.valueOf(rtnOK));
		map.put("rtnUrl", rtnUrl);
		return map;
	}

}
