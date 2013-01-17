package factory.accounts;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.accounts.PaymentSource;

/**
 * User: wangjia
 * Date: 12-10-29
 * Time: 下午5:11
 */
public class PaymentSourceFactory extends ModelFactory<PaymentSource> {
    @Override
    public PaymentSource define() {
        PaymentSource paymentSource = new PaymentSource();
        paymentSource.name = "zhifubao";
        paymentSource.detail = "zhifubao";
        paymentSource.code = "alipay";
        paymentSource.logo = "abc";
        paymentSource.showOrder = 1;
        paymentSource.paymentCode = "alipay";
        paymentSource.subPaymentCode = "alipay";
        return paymentSource;
    }
}
