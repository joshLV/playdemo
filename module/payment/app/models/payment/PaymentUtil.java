package models.payment;

import models.payment.alipay.AliPaymentFlow;
import models.payment.kuaiqian.KuaiqianPaymentFlow;
import models.payment.sina.SinaPaymentFlow;
import models.payment.tenpay.TenpayPaymentFlow;
import models.payment.test.TestPaymentFlow;

import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 * Date: 12-5-22
 */
public class PaymentUtil {
    private static PaymentFlow alipayPaymentFlow = new AliPaymentFlow();
    private static PaymentFlow tenpayPaymentFlow = new TenpayPaymentFlow();
    private static PaymentFlow kuaiqianPaymentFlow = new KuaiqianPaymentFlow();
    private static PaymentFlow sinaPaymentFlow = new SinaPaymentFlow();
    private static PaymentFlow testPaymentFlow = new TestPaymentFlow();

    public static final String PARTNER_CODE_ALIPAY  = "alipay";
    public static final String PARTNER_CODE_TENPAY  = "tenpay";
    public static final String PARTNER_CODE_99BILL  = "99bill";
    public static final String PARTNER_CODE_SINA    = "sina";
    public static final String PARTNER_CODE_TESTPAY = "testpay";

    public static PaymentFlow getPaymentFlow(String partner){
        //partner必须是payment_source表中的payment_code字段中的数据
        switch (partner){
            case PARTNER_CODE_ALIPAY:
                return alipayPaymentFlow;
            case PARTNER_CODE_TENPAY:
                return tenpayPaymentFlow;
            case PARTNER_CODE_99BILL:
                return kuaiqianPaymentFlow;
            case PARTNER_CODE_SINA:
                return sinaPaymentFlow;
            case PARTNER_CODE_TESTPAY:
                if(TestPaymentFlow.ON) { return testPaymentFlow; }
                else { return null; }
            default:
                return null;
        }
    }


    /**
     *
     * play! 会将所有参数的key-value对组合成一个额外body 存在参数列表里,所以忽略body
     * play 的route配置里我们在接受url参数时用一个{partner}来匹配不同的支付公司,结果也被play!加入了参数列表...所以忽略
     *
     * @param params play request 参数
     * @return 过滤后的参数集合
     */
    public static Map<String, String[]> filterPlayParameter(Map<String, String[]> params){
        params.remove("body");
        params.remove("shihui_partner");
        return params;
    }
}
