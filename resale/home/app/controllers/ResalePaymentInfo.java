package controllers;

import controllers.modules.resale.cas.SecureCAS;
import models.accounts.Account;
import models.accounts.PaymentSource;
import models.accounts.util.AccountUtil;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.payment.PaymentFlow;
import models.payment.PaymentJournal;
import models.payment.PaymentUtil;
import models.resale.Resaler;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

@With(SecureCAS.class)
public class ResalePaymentInfo extends Controller {

    /**
     * 展示确认支付信息页.
     *
     * @param orderNumber 订单编号
     */
    public static void index(String orderNumber) {
        //加载用户账户信息
        Resaler user = SecureCAS.getResaler();
        Account account = AccountUtil.getResalerAccount(user);

        //加载订单信息
        Order order = Order.findOneByResaler(orderNumber, user);
        long goodsNumber = OrderItems.itemsNumber(order);

        List<PaymentSource> paymentSources = PaymentSource.findAll();

        render(user, account, order, goodsNumber, paymentSources);
    }


    /**
     * 接收用户反馈的订单的支付信息.
     *
     * @param orderNumber       订单ID
     * @param useBalance        是否使用余额
     * @param paymentSourceCode 网银代码
     */
    public static void confirm(String orderNumber, boolean useBalance, String paymentSourceCode) {
        Resaler resaler = SecureCAS.getResaler();
        Order order = Order.findOneByResaler(orderNumber, resaler);
        if (order == null) {
            error(500, "no such order");
        }
        if (order.status != OrderStatus.UNPAID) {
            error("wrong order status");
        }
        Account account = AccountUtil.getResalerAccount(resaler);

        if (Order.confirmPaymentInfo(order, account, useBalance, paymentSourceCode)) {
            PaymentSource paymentSource = PaymentSource.findByCode(order.payMethod);
            render(order, paymentSource);
        } else {
            error(500, "can no confirm the payment info");
        }

    }

    /**
     * 生成网银跳转页.
     *
     * @param orderNumber 订单
     */
    public static void payIt(String orderNumber, String paymentCode) {
        Resaler resaler = SecureCAS.getResaler();
        Order order = Order.findOneByResaler(orderNumber, resaler);

        PaymentSource paymentSource = PaymentSource.findByCode(paymentCode);

        if (order == null || paymentSource == null) {
            error(500, "no such order or payment source is invalid");
            return;
        }
        if (order.status != OrderStatus.UNPAID) {
            error("wrong order status");
        }

        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow(paymentSource.paymentCode);
        if (paymentFlow == null) {
            error("payment partner not found: " + paymentSource.paymentCode);
            return;
        }
        String form = paymentFlow.getRequestForm(order.orderNumber, order.description,
                order.discountPay, paymentSource.subPaymentCode, request.remoteAddress, null);
        Logger.info("resaler payment form:" + form);
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

