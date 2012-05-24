package models.payment;

import models.payment.alipay.AliPaymentFlow;
import models.payment.kuaiqian.KuaiqianPaymentFlow;
import models.payment.tenpay.TenpayPaymentFlow;

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

    public static PaymentFlow getPaymentFlow(String partner){
        switch (partner){
            case "alipay":
                return alipayPaymentFlow;
            case "tenpay":
                return tenpayPaymentFlow;
            case "kuaiqian":
                return kuaiqianPaymentFlow;
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
        Map<String, String[]> result = new HashMap<>();
        for (Map.Entry<String, String[]> entry : params.entrySet()){
            if ("body".equals(entry.getKey()) || "partner".equals(entry.getKey())){
                continue;
            }
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
