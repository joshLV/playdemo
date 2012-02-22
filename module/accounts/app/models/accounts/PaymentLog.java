package models.accounts;

import javax.persistence.*;
import java.math.BigDecimal;

import play.db.jpa.Model;

import java.util.*;

@Entity
@Table(name="payment_log")
public class PaymentLog extends Model {

    public PaymentWay paymentWay;

    public PaymentRequest paymentRequest;

    public BigDecimal amount;

    @Column(name = "payment_status")
    public String paymentStatus;
    
    @Column(name = "return_message")
    public String returnMessage;

    @Column(name = "created_at")
    public Date createdAt;

}

