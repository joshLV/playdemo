package controllers;

import java.math.BigDecimal;
import java.util.List;

import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.accounts.util.AccountUtil;
import models.order.Order;
import models.payment.PaymentUtil;
import models.payment.PaymentFlow;
import models.resale.Resaler;

import controllers.modules.resale.cas.SecureCAS;

import play.mvc.Controller;
import play.mvc.With;

@With(SecureCAS.class)
public class Charge extends Controller{

    public static void index(){
        //加载用户账户信息
        Resaler user = SecureCAS.getResaler();
        Account account = AccountUtil.getAccount(user.getId(), AccountType.RESALER);

        List<PaymentSource> paymentSources = PaymentSource.findAll();
        render(user, account, paymentSources);
    }
    public static void create(BigDecimal amount, String paymentSourceCode){
        Resaler resaler = SecureCAS.getResaler();
        PaymentSource paymentSource = PaymentSource.findByCode(paymentSourceCode);

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

        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow(paymentSource.paymentCode);
        String form = paymentFlow.getRequestForm(order.orderNumber, order.description,
                order.discountPay, paymentSource.subPaymentCode, request.remoteAddress);

        render(form);
    }

}
