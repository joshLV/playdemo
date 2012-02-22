package models.accounts;

import javax.persistence.*;
import java.math.BigDecimal;

import play.db.jpa.Model;

import java.util.*;

@Entity
@Table(name="payment_request")
public class PaymentRequest extends Model {

    @ManyToOne
    public PaymentWay paymentWay;

    @Column(name = "serial_number")
    public String serialNumber;

    public BigDecimal amount;

    @Column(name = "created_at")
    public Date createdAt;

    public PaymentRequest(String serialNumber, 
            BigDecimal amount, PaymentWay paymentWay){
        this.serialNumber = serialNumber;
        this.amount = amount;
        this.paymentWay = paymentWay;
        this.createdAt = new Date();
    }
}

