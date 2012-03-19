package models.accounts;

import javax.persistence.*;

import play.db.jpa.Model;

@Entity
@Table(name="payment_source")
public class PaymentSource extends Model {
    public String name;
    public String detail;
    public String code;                 //银行代码
    public String logo;
    @Column(name = "show_order")
    public int showOrder;

    @Column(name = "payment_code")
    public String paymentCode;          //最终用哪个支付渠道支付
}

