package models.accounts;

import javax.persistence.*;
import java.math.BigDecimal;

import play.db.jpa.Model;

import java.util.*;

@Entity
@Table(name="payment_log")
public class PaymentLog extends Model {

    @ManyToOne
    public PaymentSource paymentSource;

    @OneToOne
    public TradeBill tradeBill;

    public BigDecimal amount;

    @Column(name = "payment_status")
    public String paymentStatus;
    
    @Column(name = "return_message")
    public String returnMessage;

    @Column(name = "created_at")
    public Date createdAt;

    public PaymentLog(PaymentSource paymentSource, TradeBill tradeBill, BigDecimal amount,
                      String paymentStatus, String returnMessage){
        this.paymentSource = paymentSource;
        this.tradeBill = tradeBill;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.returnMessage = returnMessage;
    }

}

