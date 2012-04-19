package controllers;

import java.math.BigDecimal;
import java.util.List;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import models.order.ChargeOrder;
import models.order.Order;
import models.order.OrderItems;
import models.payment.AliPaymentFlow;
import models.payment.PaymentFlow;
import models.resale.Resaler;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.resale.cas.SecureCAS;

@With(SecureCAS.class)
public class ChargePaymentInfo extends Controller {
	private static PaymentFlow paymentFlow = new AliPaymentFlow();

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
        ChargeOrder order = ChargeOrder.find("byIdAndUserIdAndAccountType", id, user.getId(), AccountType.RESALER).first();
        
        List<PaymentSource> paymentSources = PaymentSource.find("order by showOrder desc").fetch();

        render(user, account, order, paymentSources);
    }


    /**
     * 接收用户反馈的订单的支付信息.
     *
     * @param orderId           订单ID
     * @param useBalance        是否使用余额
     * @param paymentSourceCode 网银代码
     */
    public static void confirm(long orderId, String paymentSourceCode) {
        Resaler resaler = SecureCAS.getResaler();
        System.out.println("dsdfs" + resaler == null);
        ChargeOrder order = ChargeOrder.find("byIdAndUserIdAndAccountType", orderId, resaler.getId(), AccountType.RESALER).first();
        if (order == null){
            error(500,"no such order");
        }
        Account account = AccountUtil.getAccount(resaler.getId(), AccountType.RESALER);
 
        PaymentSource paymentSource = PaymentSource.find("byCode", paymentSourceCode).first();
        //无法确定支付渠道
        if(paymentSource == null){
            error(500, "can not get paymentSource");
        }

        //创建订单交易
        TradeBill tradeBill =
                TradeUtil.createChargeTrade(account, order.chargeAmount, paymentSource);
        if(tradeBill == null){
            error(500, "error create trade bill");
        }
        
        /*网银付款*/
        order.tradeId = tradeBill.getId();
        order.paymentSource = paymentSourceCode;
        order.save();
        render(order, paymentSource);

    }

    /**
     * 生成网银跳转页.
     *
     * @param orderId               订单
     */
    public static void payIt(long orderId){
        Resaler resaler = SecureCAS.getResaler();
        ChargeOrder order = ChargeOrder.find("byIdAndUserIdAndAccountType", orderId, resaler.getId(), AccountType.RESALER).first();

        if (order == null){
            error(500,"no such order");
        }

        String form = paymentFlow.generateChargeForm(order);
        render(form);
    }


}

