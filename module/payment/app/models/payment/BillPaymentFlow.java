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
import models.order.Order;
import models.order.OrderStatus;
import play.Logger;
import thirdpart.alipay.config.AlipayConfig;

public class BillPaymentFlow {
	public static Map<String, String> billPara = new HashMap();

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
		String trade_no = order.orderNumber;
		//订单名称，显示在支付宝收银台里的“商品名称”里，显示在支付宝的交易管理的
		//“商品名称”的列表里。
		String subject = null;
		if(order.orderItems.size() == 1){
			subject = order.orderItems.get(0).goodsName ;
		}else if(order.orderItems.size() > 1){
			subject = order.orderItems.get(0).goodsName + "等商品";
		}
		//订单总金额，显示在支付宝收银台里的“应付总额”里
		String orderAmount= String.valueOf(order.amount.multiply(new BigDecimal(100)).divide(new BigDecimal(1),0,BigDecimal.ROUND_HALF_UP));

		String bankId=order.payMethod;
		//订单提交时间divide(1,0,BigDecimal.ROUND_HALF_UP))
		///14位数字。年[4位]月[2位]日[2位]时[2位]分[2位]秒[2位]
		///如；20080101010101
		String orderTime=systemTime(order.createdAt);

		//人民币网关账户号
		///请登录快钱系统获取用户编号，用户编号后加01即为人民币网关账户号。//1002034040901
		String merchantAcctId="1002034040901";

		//人民币网关密钥
		///区分大小写.请与快钱联系索取
		String key="C9TW75QQNULLII8B";

		//字符集.固定选择值。可为空。
		///只能选择1、2、3.
		///1代表UTF-8; 2代表GBK; 3代表gb2312
		///默认值为1
		String inputCharset="1";

		//接受支付结果的页面地址.与[bgUrl]不能同时为空。必须是绝对地址。
		///如果[bgUrl]为空，快钱将支付结果Post到[pageUrl]对应的地址。
		///如果[bgUrl]不为空，并且[bgUrl]页面指定的<redirecturl>地址不为空，则转向到<redirecturl>对应的地址
		String pageUrl="";

		//服务器接受支付结果的后台地址.与[pageUrl]不能同时为空。必须是绝对地址。
		///快钱通过服务器连接的方式将交易结果发送到[bgUrl]对应的页面地址，在商户处理完成后输出的<result>如果为1，页面会转向到<redirecturl>对应的地址。
		///如果快钱未接收到<redirecturl>对应的地址，快钱将把支付结果post到[pageUrl]对应的页面。
		String bgUrl = "http://www.uhuila.cn:9001/pay/bill_notify";
		//网关版本.固定值
		///快钱会根据版本号来调用对应的接口处理程序。
		///本代码版本号固定为v2.0
		String version="v2.0";

		//语言种类.固定选择值。
		///只能选择1、2、3
		///1代表中文；2代表英文
		///默认值为1
		String language="1";

		//签名类型.固定值
		///1代表MD5签名
		///当前版本固定为1
		String signType="4";

		//支付人姓名
		///可为中文或英文字符
		String payerName="payerName";

		//支付人联系方式类型.固定选择值
		///只能选择1
		///1代表Email
		String payerContactType="1";

		//支付人联系方式
		///只能选择Email或手机号
		String payerContact="";
		System.out.println("subject============================"+subject);
		//商品名称
		///可为中文或英文字符
		String productName=subject;

		//商品代码
		///可为字符或者数字
		String productId="";

		//商品描述
		String productDesc="";

		//扩展字段1
		///在支付结束后原样返回给商户
		String ext1="";

		//扩展字段2
		///在支付结束后原样返回给商户
		String ext2="";

		//支付方式.固定选择值
		///只能选择00、10、11、12、13、14
		///00：组合支付（网关支付页面显示快钱支持的各种支付方式，推荐使用）10：银行卡支付（网关支付页面只显示银行卡支付）.11：电话银行支付（网关支付页面只显示电话支付）.12：快钱账户支付（网关支付页面只显示快钱账户支付）.13：线下支付（网关支付页面只显示线下支付方式）.14：B2B支付（网关支付页面只显示B2B支付，但需要向快钱申请开通才能使用）
		String payType="10";

		//商品数量
		///可为空，非空时必须为数字
		String productNum="";

		//同一订单禁止重复提交标志
		///固定选择值： 1、0
		///1代表同一订单号只允许提交1次；0表示同一订单号在没有支付成功的前提下可重复提交多次。默认为0建议实物购物车结算类商户采用0；虚拟产品类商户采用1
		String redoFlag="1";

		//快钱的合作伙伴的账户号
		///如未和快钱签订代理合作协议，不需要填写本参数
		String pid="";


		//生成加密签名串
		///请务必按照如下顺序和规则组成加密串！
		String signMsgVal="";
		signMsgVal=appendParam(signMsgVal,"inputCharset",inputCharset);
		signMsgVal=appendParam(signMsgVal,"pageUrl",pageUrl);
		signMsgVal=appendParam(signMsgVal,"bgUrl",bgUrl);
		signMsgVal=appendParam(signMsgVal,"version",version);
		signMsgVal=appendParam(signMsgVal,"language",language);
		signMsgVal=appendParam(signMsgVal,"signType",signType);
		signMsgVal=appendParam(signMsgVal,"merchantAcctId",merchantAcctId);
		signMsgVal=appendParam(signMsgVal,"payerName",payerName);
		signMsgVal=appendParam(signMsgVal,"payerContactType",payerContactType);
		signMsgVal=appendParam(signMsgVal,"payerContact",payerContact);
		signMsgVal=appendParam(signMsgVal,"orderId",trade_no);
		signMsgVal=appendParam(signMsgVal,"orderAmount",orderAmount);
		signMsgVal=appendParam(signMsgVal,"orderTime",orderTime);
		signMsgVal=appendParam(signMsgVal,"productName",productName);
		signMsgVal=appendParam(signMsgVal,"productNum",productNum);
		signMsgVal=appendParam(signMsgVal,"productId",productId);
		signMsgVal=appendParam(signMsgVal,"productDesc",productDesc);
		signMsgVal=appendParam(signMsgVal,"ext1",ext1);
		signMsgVal=appendParam(signMsgVal,"ext2",ext2);
		signMsgVal=appendParam(signMsgVal,"payType",payType);
		signMsgVal=appendParam(signMsgVal,"bankId",bankId);
		signMsgVal=appendParam(signMsgVal,"redoFlag",redoFlag);
		signMsgVal=appendParam(signMsgVal,"pid",pid);
		//signMsgVal=appendParam(signMsgVal,"key",key);

		//String signMsg=MD5md5Hex(signMsgVal.getBytes("gb2312)).toUpperCase();
		// 进行PKI加密
		String signMsg = Pkipair.signMsg(signMsgVal);

		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("inputCharset", inputCharset);
		sParaTemp.put("pageUrl", pageUrl);
		sParaTemp.put("bgUrl", bgUrl);
		sParaTemp.put("version", version);
		sParaTemp.put("language", language);	
		sParaTemp.put("signType", signType);
		sParaTemp.put("signMsg", signMsg);
		sParaTemp.put("merchantAcctId", merchantAcctId);
		sParaTemp.put("payerName", payerName);
		sParaTemp.put("payerContactType", payerContactType);
		sParaTemp.put("payerContact", payerContact);
		sParaTemp.put("orderId", trade_no);	
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
		
		buildForm(sParaTemp);
		return buildForm(sParaTemp);      
	}


	/**
	 * 构造提交表单HTML数据
	 * @param sParaTemp 请求参数数组
	 * @param gateway 网关地址
	 * @param strMethod 提交方式。两个值可选：post、get
	 * @param strButtonName 确认按钮显示文字
	 * @return 提交表单HTML文本
	 */
	public static String buildForm(Map<String, String> sParaTemp) {
		List<String> keys = new ArrayList<String>(sParaTemp.keySet());

		StringBuffer sbHtml = new StringBuffer();

		sbHtml.append("<form id='kqPay' name='kqPay' action='https://www.99bill.com/gateway/recvMerchantInfoAction.htm' method='post' >");

		for (int i = 0; i < keys.size(); i++) {
			String name = (String) keys.get(i);
			String value = (String) sParaTemp.get(name);
			sbHtml.append("<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>");
		}

		sbHtml.append("</form><script>document.forms['kqPay'].submit();</script>");

		return sbHtml.toString();
	}

	public String appendParam(String returns, String paramId, String paramValue) {
		if (returns != "") {
			if (paramValue != "") {
				returns += "&" + paramId + "=" + paramValue;
			}
		} else {
			if (paramValue != "") {
				returns = paramId + "=" + paramValue;
			}
		}

		return returns;
	}

	/**
	 * 回调接口，处理订单信息
	 * 
	 * @param params 参数
	 * @return 处理成功或失败
	 */
	public boolean paymentNotify(Map<String, String[]> params) {
		System.out.println(">>>>>>>>>>>>>>>>."+params);
		//获取人民币网关账户号
		String merchantAcctId = params.get("merchantAcctId") == null ? null : params.get("merchantAcctId")[0];

		//获取网关版本.固定值
		///快钱会根据版本号来调用对应的接口处理程序。
		///本代码版本号固定为v2.0
		String version = params.get("version") == null ? null : params.get("version")[0];

		//获取语言种类.固定选择值。
		///只能选择1、2、3
		///1代表中文；2代表英文
		///默认值为1
		String language = params.get("language") == null ? null : params.get("language")[0];

		//签名类型.固定值
		///1代表MD5签名
		///当前版本固定为1
		String signType = params.get("signType") == null ? null : params.get("signType")[0];

		//获取支付方式
		///值为：10、11、12、13、14
		///00：组合支付（网关支付页面显示快钱支持的各种支付方式，推荐使用）10：银行卡支付（网关支付页面只显示银行卡支付）.11：电话银行支付（网关支付页面只显示电话支付）.12：快钱账户支付（网关支付页面只显示快钱账户支付）.13：线下支付（网关支付页面只显示线下支付方式）.14：B2B支付（网关支付页面只显示B2B支付，但需要向快钱申请开通才能使用）
		String payType = params.get("payType") == null ? null : params.get("payType")[0];
		//获取银行代码
		///参见银行代码列表
		String bankId = params.get("bankId") == null ? null : params.get("bankId")[0];

		//获取商户订单号
		String orderNo = params.get("orderId") == null ? null : params.get("orderId")[0];

		//获取订单提交时间
		///获取商户提交订单时的时间.14位数字。年[4位]月[2位]日[2位]时[2位]分[2位]秒[2位]
		///如：20080101010101
		String orderTime = params.get("orderTime") == null ? null : params.get("orderTime")[0];

		//获取原始订单金额
		///订单提交到快钱时的金额，单位为分。
		///比方2 ，代表0.02元
		String orderAmount = params.get("orderAmount") == null ? null : params.get("orderAmount")[0];

		//获取快钱交易号
		///获取该交易在快钱的交易号
		String dealId = params.get("dealId") == null ? null : params.get("dealId")[0];

		//获取银行交易号
		///如果使用银行卡支付时，在银行的交易号。如不是通过银行支付，则为空
		String bankDealId = params.get("bankDealId") == null ? null : params.get("bankDealId")[0];

		//获取在快钱交易时间
		///14位数字。年[4位]月[2位]日[2位]时[2位]分[2位]秒[2位]
		///如；20080101010101
		String dealTime = params.get("dealTime") == null ? null : params.get("dealTime")[0];

		//获取实际支付金额
		///单位为分
		///比方 2 ，代表0.02元
		String payAmount = params.get("payAmount") == null ? null : params.get("payAmount")[0];

		//获取交易手续费
		///单位为分
		///比方 2 ，代表0.02元
		String fee = params.get("fee") == null ? null : params.get("fee")[0];
		//获取扩展字段1
		String ext1 = params.get("ext1") == null ? null : params.get("ext1")[0];
		//获取扩展字段2
		String ext2 = params.get("ext2") == null ? null : params.get("ext2")[0];
		//获取处理结果
		///10代表 成功11代表 失败
		///00代表 下订单成功（仅对电话银行支付订单返回）;01代表 下订单失败（仅对电话银行支付订单返回）
		String payResult = params.get("payResult") == null ? null : params.get("payResult")[0];
		//获取错误代码
		///详细见文档错误代码列表
		String errCode = params.get("errCode") == null ? null : params.get("errCode")[0];
		//获取加密签名串
		String signMsg = params.get("signMsg") == null ? null : params.get("signMsg")[0];
		//生成加密串。必须保持如下顺序。
		String merchantSignMsgVal="";
		merchantSignMsgVal=appendParam(merchantSignMsgVal,"merchantAcctId",merchantAcctId);
		merchantSignMsgVal=appendParam(merchantSignMsgVal,"version",version);
		merchantSignMsgVal=appendParam(merchantSignMsgVal,"language",language);
		merchantSignMsgVal=appendParam(merchantSignMsgVal,"signType",signType);
		merchantSignMsgVal=appendParam(merchantSignMsgVal,"payType",payType);
		merchantSignMsgVal=appendParam(merchantSignMsgVal,"bankId",bankId);
		merchantSignMsgVal=appendParam(merchantSignMsgVal,"orderId",orderNo);
		merchantSignMsgVal=appendParam(merchantSignMsgVal,"orderTime",orderTime);
		merchantSignMsgVal=appendParam(merchantSignMsgVal,"orderAmount",orderAmount);
		merchantSignMsgVal=appendParam(merchantSignMsgVal,"dealId",dealId);
		merchantSignMsgVal=appendParam(merchantSignMsgVal,"bankDealId",bankDealId);
		merchantSignMsgVal=appendParam(merchantSignMsgVal,"dealTime",dealTime);
		merchantSignMsgVal=appendParam(merchantSignMsgVal,"payAmount",payAmount);
		merchantSignMsgVal=appendParam(merchantSignMsgVal,"fee",fee);
		merchantSignMsgVal=appendParam(merchantSignMsgVal,"ext1",ext1);
		merchantSignMsgVal=appendParam(merchantSignMsgVal,"ext2",ext2);
		merchantSignMsgVal=appendParam(merchantSignMsgVal,"payResult",payResult);
		merchantSignMsgVal=appendParam(merchantSignMsgVal,"errCode",errCode);
		// 响应验签
		boolean b = Pkipair.enCodeByCer(merchantSignMsgVal, signMsg);
		System.out.println("QQQQQQQQQQQQQQQQQQQQQQQQQQ");
		//初始化结果及地址
		int rtnOk=0;
		String rtnUrl = "http://www.uhuila.cn:9001/orders/billpay_result";
		//商家进行数据处理，并跳转会商家显示支付结果的页面
		///首先进行签名字符串验证
		boolean success = true;
		if(b){

			String log = "tenpay_notify:" +
					"交易状态:" + payResult + "," +
					"交易号:" + bankDealId + "," +
					"订单号:" + orderNo + "," +
					"总金额:" + orderAmount + "," +
					"商品名称:" + "";
			Logger.info(log);
			PaymentCallbackLog callbackLog =
					new PaymentCallbackLog("afasfa", "tenpay", orderNo,new BigDecimal(orderAmount), payResult, log);
			///接着进行支付结果判断
			switch(Integer.parseInt(payResult)){

			case 10:
				Order order = Order.find("byOrderNumber",orderNo).first();
				System.out.println("333333333333333333333");
				if (order == null || order.orderNumber == null ) {
					Logger.error("tenpay_notify:没有此订单信息！");
					callbackLog.status = "invalid_trade";
					rtnUrl+="?rtnOk=-4";
					rtnOk=1;
					success = false;
					break;
				}
				System.out.println("4444444444444444444444");
				BigDecimal amount= 	order.amount.multiply(new BigDecimal(100)).divide(new BigDecimal(1),0,BigDecimal.ROUND_HALF_UP);

				//订单支付金额不相符
				if(amount.compareTo(new BigDecimal(orderAmount)) !=0) {
					Logger.error("tenpay_notify:订单支付金额不相符！");
					callbackLog.status = "invalid_order_money";
					rtnOk=1;
					rtnUrl+="?rtnOk=-4";
					success = false;
					break;
				}

				if (OrderStatus.PAID.equals(order.status)) {
					Logger.error("tenpay_notify:订单已被处理:" + orderNo);
					callbackLog.status = "processed";
				} else if (success){
					//					Long tradeId = order.payRequestId;
					//					TradeBill tradeBill = TradeBill.findById(tradeId);
					//					if(tradeBill != null){
					//						//最终所有条件满足
					//						TradeUtil.success(tradeBill);
					//						order.paid();
					//					} else {
					//						callbackLog.status = "no_trade_found";
					//						success = false;
					//					}
					rtnOk=1;
				}

			default:
				rtnOk=-5;
				rtnUrl="";
				break;
			}		  
			billPara.put("rtnOk", String.valueOf(rtnOk));
			billPara.put("rtnUrl", rtnUrl);
		} else {
			Logger.error("tenpay_notify:认证签名失败");
			success = false;
		}


		return success;
	}
}
