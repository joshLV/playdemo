package models.accounts;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 第三方支付
 *
 * @author likang
 */
@Entity
@Table(name = "payment_source")
public class PaymentSource extends Model {
    public String name;
    public String detail;
    public String code;                 //全表唯一的银行代码,一般是paymentCode-subPaymentCode
    public String logo;
    @Column(name = "show_order")
    public int showOrder;

    @Column(name = "payment_code")
    public String paymentCode;          //最终用哪个支付渠道支付

    @Column(name = "sub_payment_code")
    public String subPaymentCode;       //支付渠道中使用的哪个支付方式(银行)


    public static String findNameByCode(String code) {
        PaymentSource source = PaymentSource.find("byCode", code).first();
        return source == null ? "" : source.name;
    }

    public static PaymentSource findByCode(String code){
        return PaymentSource.find("byCode", code).first();
    }

    public static PaymentSource getBalanceSource(){
        PaymentSource source = PaymentSource.find("byCode", "balance").first();
        if(source == null){
            source = new PaymentSource();
            source.name = "余额支付";
            source.detail = "余额支付";
            source.code = "balance";
            source.logo = null;
            source.showOrder = 0;
            source.paymentCode = "balance";
            source.subPaymentCode = "balance";
            source.save();
        }
        return source;
    }
}

