package unit;

import java.math.BigDecimal;

import models.payment.PaymentFlow;
import models.payment.PaymentUtil;

import org.junit.Test;

import play.test.UnitTest;

/**
 * @author likang
 * Date: 12-6-5
 */
public class PaymentFlowTest extends UnitTest{

    @Test
    public void testAlipayForm(){
        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow("alipay");
        assertNotNull(paymentFlow);
        assertNotNull(paymentFlow.getRequestForm("002100", "alipay order test", new BigDecimal("10"), "alipay", "127.0.0.1", null));
    }

    @Test
    public void testTenpayForm(){
        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow("tenpay");
        assertNotNull(paymentFlow);
        assertNotNull(paymentFlow.getRequestForm("002100", "tenpay order test", new BigDecimal("10"), "tenpay", "127.0.0.1", null));
    }
    @Test
    public void test99BillForm(){
        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow("99bill");
        assertNotNull(paymentFlow);
        assertNotNull(paymentFlow.getRequestForm("002100", "99bill order test", new BigDecimal("10"), "CMB", "127.0.0.1", null));
    }
}
