package controllers;

import controllers.modules.webcas.WebCAS;
import models.accounts.util.TradeUtil;
import models.consumer.*;
import models.accounts.*;
import models.order.*;
import models.payment.AliPaymentFlow;
import models.payment.PaymentFlow;
import models.sms.SMSUtil;
import play.mvc.Controller;
import play.mvc.With;

import thirdpart.alipay.services.*;

import java.math.BigDecimal;
import java.util.*;

import controllers.modules.cas.SecureCAS;

@With({SecureCAS.class, WebCAS.class})
public class PaymentInfo extends Controller {
	private static PaymentFlow paymentFlow = new AliPaymentFlow();

    /**
     * 展示确认支付信息页.
     *
     * @param id 订单ID
     */
    public static void index(long id) {
        //加载用户账户信息
        User user = WebCAS.getUser();
        Account account = Account.find("byUid", user.getId()).first();

        //加载订单信息
        Order order = Order.find("byIdAndUser", id, user).first();
        long goodsNumber = OrderItems.itemsNumber(order);
        
        List<PaymentSource> paymentSources = PaymentSource.find("order by order desc").fetch();

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
        User user = WebCAS.getUser();
        Order order = Order.find("byIdAndUser", orderId, user).first();
        
        if (order == null){
            error(500,"no such order");
        }

        Account account = Account.find("byUid", user.getId()).first();

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
        TradeBill tradeBill =
                TradeUtil.createOrderTrade(account, balancePaymentAmount,ebankPaymentAmount,paymentSource, orderId);
        if(tradeBill == null){
            error(500, "error create trade bill");
        }
        order.payRequestId = tradeBill.getId();
        order.payMethod = paymentSourceCode;

        //如果使用余额足以支付，则付款直接成功
        if (ebankPaymentAmount.compareTo(BigDecimal.ZERO) == 0){
            TradeUtil.success(tradeBill);
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
    public static void payIt(long orderId){
        User user = WebCAS.getUser();
        Order order = Order.find("byIdAndUser", orderId, user).first();

        if (order == null){
            error(500,"no such order");
        }

        String form = paymentFlow.generateForm(order);
        render(form);

    }


}

