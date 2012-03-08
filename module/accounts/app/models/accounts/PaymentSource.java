package models.accounts;

import javax.persistence.*;
import java.math.BigDecimal;

import play.db.jpa.Model;

@Entity
@Table(name="payment_source")
public class PaymentSource extends Model {
    public String name;
    public String detail;
    public String code;
    public String logo;
    public int order;

    public PaymentSource(String name, String detail, String code, String logo){
        this.name   = name;
        this.detail = detail;
        this.logo   = logo;
        this.code = code;
        this.order = 0;
    }
}

