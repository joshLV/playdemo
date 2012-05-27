package controllers;

import java.math.BigDecimal;
import java.util.List;

import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.accounts.util.AccountUtil;
import models.consumer.User;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrderType;
import models.payment.PaymentJournal;
import models.payment.alipay.AliPaymentFlow;
import models.payment.kuaiqian.KuaiqianPaymentFlow;
import models.payment.PaymentFlow;
import models.payment.PaymentUtil;
import models.payment.tenpay.TenpayPaymentFlow;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.website.cas.SecureCAS;

@With({SecureCAS.class, WebsiteInjector.class})
public class PaymentInfo extends Controller {

	/**
	 * 展示确认支付信息页.
	 *
	 * @param orderNumber 订单ID
	 */
	public static void index(String orderNumber) {
		//加载用户账户信息
		User user = SecureCAS.getUser();
		Account account = AccountUtil.getAccount(user.getId(), AccountType.CONSUMER);

		//加载订单信息
		Order order = Order.findOneByUser(orderNumber, user.getId(), AccountType.CONSUMER);
		long goodsNumber = OrderItems.itemsNumber(order);

		List<PaymentSource> paymentSources = PaymentSource.find("order by showOrder").fetch();

		render(user, account, order, goodsNumber, paymentSources);
	}


	/**
	 * 接收用户反馈的订单的支付信息.
	 *
	 * @param orderNumber       订单ID
	 * @param useBalance        是否使用余额
	 * @param paymentSourceCode 网银代码
	 */
	public static void confirm(String  orderNumber, boolean useBalance, String paymentSourceCode) {
		User user = SecureCAS.getUser();
		Order order = Order.findOneByUser(orderNumber, user.getId(), AccountType.CONSUMER);
        if (order == null){
            error(500,"no such order");
        }
        Account account = AccountUtil.getAccount(user.getId(), AccountType.CONSUMER);


        if(Order.confirmPaymentInfo(order, account, useBalance, paymentSourceCode)){
            PaymentSource paymentSource = PaymentSource.findByCode(order.payMethod);
            render(order, paymentSource);
        }else {
            error(500, "can no confirm the payment info");
        }
	}

	/**
	 * 生成网银跳转页.
	 *
	 * @param orderNumber 订单
	 */
	public static void payIt(String orderNumber,String paymentCode){
		User user = SecureCAS.getUser();
		Order order = Order.findOneByUser(orderNumber, user.getId(), AccountType.CONSUMER);
        PaymentSource paymentSource = PaymentSource.findByCode(paymentCode);

		if (order == null || paymentSource == null){
			error(500,"no such order or payment source is invalid");
            return;
		}

        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow(paymentSource.paymentCode);
        String form = paymentFlow.getRequestForm(order.orderNumber, order.description,
                order.discountPay, paymentSource.subPaymentCode, request.remoteAddress);

        PaymentJournal.savePayRequestJournal(
                order.orderNumber,
                order.description,
                order.discountPay.toString(),
                paymentSource.paymentCode,
                paymentSource.subPaymentCode,
                request.remoteAddress,
                form);
        render(form);
	}
}

