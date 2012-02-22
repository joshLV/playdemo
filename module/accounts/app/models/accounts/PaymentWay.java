package models.accounts;

import javax.persistence.*;
import java.math.BigDecimal;

import play.db.jpa.Model;

@Entity
@Table(name="payment_way")
public class PaymentWay extends Model {
    public String name;
    public String detail;
    public long port;
    public String logo;

    public PaymentWay(String name, String detail, long port, String logo){
        this.name   = name;
        this.detail = detail;
        this.port   = port;
        this.logo   = logo;
    }
}

