package models.accounts;

import javax.persistence.*;
import java.math.BigDecimal;

import play.db.jpa.Model;

import java.util.*;

@Entity
@Table(name="payment_log")
public class PaymentCallbackLog extends Model {
    
    public String account;

    @Column(name = "payment_source_code")
    public String paymentSourceCode;

    @Column(name = "trade_bill")
    public String tradeBill;

    public BigDecimal amount;

    public String status;

    @Lob
    public String message;

    @Column(name = "created_at")
    public Date createdAt;

    public PaymentCallbackLog(String account, String paymentSourceCode, String tradeBill, BigDecimal amount,
                              String status, String message){
        this.account = account;        
        this.paymentSourceCode = paymentSourceCode;
        this.tradeBill = tradeBill;
        this.amount = amount;
        this.status = status;
        this.message = message;
        this.createdAt = new Date();
    }

}

