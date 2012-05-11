package controllers;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.List;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import models.order.Order;
import models.order.OrderItems;
import models.payment.AliPaymentFlow;
import models.payment.BillPaymentFlow;
import models.payment.PaymentFlow;
import models.payment.TenpayPaymentFlow;
import models.resale.Resaler;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.resale.cas.SecureCAS;

@With(SecureCAS.class)
public class PaymentInfo extends Controller {
	private static PaymentFlow alipayPaymentFlow = new AliPaymentFlow();
	private static TenpayPaymentFlow tenpayPaymentFlow = new TenpayPaymentFlow();
	private static BillPaymentFlow billPaymentFlow = new BillPaymentFlow();

    /**
     * 展示确认支付信息页.
     *
     * @param id 订单ID
     */
    public static void index(long id) {
        //加载用户账户信息
        Resaler user = SecureCAS.getResaler();
        Account account = AccountUtil.getAccount(user.getId(), AccountType.RESALER);

        //加载订单信息
        Order order = Order.find("byIdAndUserIdAndUserType", id, user.getId(), AccountType.RESALER).first();
        long goodsNumber = OrderItems.itemsNumber(order);
        
        List<PaymentSource> paymentSources = PaymentSource.findAll();

        render(user, account, order, goodsNumber, paymentSources);
    }


    /**
     * 接收用户反馈的订单的支付信息.
     *
     * @param orderId           订单ID
     * @param useBalance        是否使用余额
     * @param paymentSourceCode 网银代码
     */
    public static void confirm(long orderId, boolean useBalance, String paymentSourceCode) {
        Resaler resaler = SecureCAS.getResaler();
        Order order = Order.find("byIdAndUserIdAndUserType", orderId, resaler.getId(), AccountType.RESALER).first();
        
        if (order == null){
            error(500,"no such order");
        }

        Account account = AccountUtil.getAccount(resaler.getId(), AccountType.RESALER);

        //计算使用余额支付和使用银行卡支付的金额
        BigDecimal balancePaymentAmount = BigDecimal.ZERO;
        BigDecimal ebankPaymentAmount = BigDecimal.ZERO;
        if (useBalance){
            balancePaymentAmount = account.amount.min(order.needPay);
            ebankPaymentAmount = order.needPay.subtract(balancePaymentAmount);
        }else {
            ebankPaymentAmount = order.needPay;
        }
        order.accountPay = balancePaymentAmount;
        order.discountPay = ebankPaymentAmount;

        //创建订单交易
        PaymentSource paymentSource = PaymentSource.find("byCode", paymentSourceCode).first();

        order.payMethod = paymentSourceCode;

        //如果使用余额足以支付，则付款直接成功
        if (ebankPaymentAmount.compareTo(BigDecimal.ZERO) == 0){
            order.payMethod = PaymentSource.getBalanceSource().code;
            order.paid();

            render(order,paymentSource);
        }

        /*网银付款*/

        //无法确定支付渠道
        if(paymentSource == null){
            error(500, "can not get paymentSource");
        }

        order.save();
        render(order, paymentSource);

    }

    /**
     * 生成网银跳转页.
     *
     * @param orderId               订单
     */
    public static void payIt(long orderId,String paymentCode){
        Resaler resaler = SecureCAS.getResaler();
        Order order = Order.find("byIdAndUserIdAndUserType", orderId, resaler.getId(), AccountType.RESALER).first();

        if (order == null){
            error(500,"no such order");
        }

    	String form = "";
		if ("tenpay".equals(paymentCode)) {
			try {
				form = tenpayPaymentFlow.generatetTenpayForm(order);
				//直接跳转到财付通
				redirect(form);
			} catch (UnsupportedEncodingException e) {
				error(500,"no such order");
			}
		} else if ("alipay".equals(paymentCode)) {
			form = alipayPaymentFlow.generateForm(order);
			render(form);
		} else {
			form = billPaymentFlow.generateForm(order);
			render(form);
		}

    }


}

