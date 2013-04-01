package controllers;


import com.google.gson.Gson;
import controllers.modules.website.cas.SecureCAS;
import models.accounts.AccountType;
import models.consumer.User;
import models.order.Order;
import models.order.OrderStatus;
import models.payment.PaymentFlow;
import models.payment.PaymentJournal;
import models.payment.PaymentUtil;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

import javax.persistence.OptimisticLockException;
import java.util.List;
import java.util.Map;

@With({SecureCAS.class, WebsiteInjector.class})
public class OrderResult extends Controller {

    /**
     * 支付 url 跳转回来.
     *
     * @param shihui_partner 第三方支付
     */
    public static void urlReturn(String shihui_partner) {
        Logger.info(new Gson().toJson(params.allSimple()));
        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow(shihui_partner);
        if(paymentFlow == null) {
            error("payment partner not found: " + shihui_partner);
        }
        Map<String, String[]> allParams = params.all();
        Map<String, String> result = paymentFlow.urlReturn(PaymentUtil.filterPlayParameter(allParams));
        String errorMessage = "对不起，暂时无法读取信息，请您稍后再试";

        String orderNumber = result.get(PaymentFlow.ORDER_NUMBER);
        String fee = result.get(PaymentFlow.TOTAL_FEE);
        boolean success = false;
        if (PaymentFlow.VERIFY_RESULT_OK.equals(result.get(PaymentFlow.VERIFY_RESULT))) {
            boolean processOrderResult = false;
            try {
                processOrderResult = Order.verifyAndPay(orderNumber, fee, result.get(PaymentFlow.PAYMENT_CODE));
            }catch (OptimisticLockException e) {
                //乐观锁异常，通常是因为第三方支付已在后台回调了支付成功的接口
                Order order = Order.find("byOrderNumber", orderNumber).first();
                order.refresh();
                if(order.status == OrderStatus.PAID) {
                    processOrderResult = true;
                }
            }

            if (processOrderResult) {
                errorMessage = null;
                success = true;
            }
        }
        User user = SecureCAS.getUser();
        PaymentJournal.saveUrlReturnJournal(orderNumber, params.all(), result, success);
        
        Order order = Order.findOneByUser(orderNumber, user.getId(), AccountType.CONSUMER);

        if (shihui_partner.equals(PaymentUtil.PARTNER_CODE_SINA)) {
            String[] ext = allParams.get("source");
            if (ext != null && ext.length > 0 && ext[0].equals("wap")) {
                renderTemplate("WebSinaVouchers/payResult.html", errorMessage, order, orderNumber);
            }
        }
        //近日成交商品
        List<models.sales.Goods> recentGoodsList = models.sales.Goods.findTradeRecently(5);

        renderTemplate("OrderResult/index.html", errorMessage,order, orderNumber, recentGoodsList);
    }
}
