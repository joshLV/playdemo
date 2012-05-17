package controllers;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.List;

import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.accounts.util.AccountUtil;
import models.order.Order;
import models.payment.AliPaymentFlow;
import models.payment.BillPaymentFlow;
import models.payment.PaymentFlow;
import models.payment.TenpayPaymentFlow;
import models.resale.Resaler;

import controllers.modules.resale.cas.SecureCAS;

import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

@With(SecureCAS.class)
public class Charge extends Controller{
    private static PaymentFlow alipayPaymentFlow = new AliPaymentFlow();
    private static TenpayPaymentFlow tenpayPaymentFlow = new TenpayPaymentFlow();
    private static BillPaymentFlow billPaymentFlow = new BillPaymentFlow();

    public static void index(){
        //加载用户账户信息
        Resaler user = SecureCAS.getResaler();
        Account account = AccountUtil.getAccount(user.getId(), AccountType.RESALER);

        List<PaymentSource> paymentSources = PaymentSource.findAll();
        render(user, account, paymentSources);
    }
    public static void create(BigDecimal amount, String paymentSourceCode){
        Resaler resaler = SecureCAS.getResaler();
        PaymentSource paymentSource = PaymentSource.find("byCode", paymentSourceCode).first();

        if(amount == null || amount.compareTo(BigDecimal.ONE) < 0){
            error("invalid amount");
        }
        if (paymentSource == null){
            error("invalid payment source");
        }
        amount = amount.setScale(2);

        Order order = Order.createChargeOrder(resaler.getId(), AccountType.RESALER );
        order.amount = amount;
        order.needPay = amount;
        order.accountPay = BigDecimal.ZERO;
        order.discountPay = amount;

        order.payMethod = paymentSourceCode;
        order.generateOrderDescription();
        order.save();

        String form;
        if ("tenpay".equals(paymentSourceCode)) {
            try {
                form = tenpayPaymentFlow.generatetTenpayForm(order);
                //直接跳转到财付通
                redirect(form);
            } catch (UnsupportedEncodingException e) {
                error(500,"no such order");
            }
        } else if ("alipay".equals(paymentSourceCode)) {
            form = alipayPaymentFlow.generateForm(order);
            render(form);
        } else {
            form = billPaymentFlow.generateForm(order);
            render(form);
        }
    }

}
