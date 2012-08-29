package controllers;

import controllers.modules.resale.cas.SecureCAS;
import models.order.Order;
import models.order.OrderStatus;
import models.payment.PaymentJournal;
import models.payment.PaymentUtil;

import models.payment.PaymentFlow;
import play.mvc.Controller;
import play.mvc.With;

import javax.persistence.OptimisticLockException;
import java.util.Map;

@With(SecureCAS.class)
public class OrderResult extends Controller {

    /**
     * 支付 url 跳转回来.
     *
     * @param shihui_partner 第三方支付代码
     */
    public static void urlReturn(String shihui_partner) {
        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow(shihui_partner);
        if(paymentFlow == null) {
            error("payment partner not found: " + shihui_partner);
            return;
        }
        Map<String, String> result = paymentFlow.urlReturn(PaymentUtil.filterPlayParameter(params.all()));
        String  errorMessage = "对不起，暂时无法读取信息，请您稍后再试";

        String orderNumber = result.get(PaymentFlow.ORDER_NUMBER);
        String fee         = result.get(PaymentFlow.TOTAL_FEE);
        boolean success = false;
        if(PaymentFlow.VERIFY_RESULT_OK.equals(result.get(PaymentFlow.VERIFY_RESULT))){
            boolean processOrderResult = false;
            try {
                processOrderResult = Order.verifyAndPay(orderNumber, fee);
            }catch (OptimisticLockException e) {
                //乐观锁异常，通常是因为第三方支付已在后台回调了支付成功的接口
                Order order = Order.find("byOrderNumber", orderNumber).first();
                order.refresh();
                if(order.status == OrderStatus.PAID) {
                    processOrderResult = true;
                }
            }

            if(processOrderResult){
                errorMessage = null;
                success = true;
            }
        }
        PaymentJournal.saveUrlReturnJournal(orderNumber, params.all(), result, success);
        renderTemplate("OrderResult/index.html", errorMessage, orderNumber);
    }
}
