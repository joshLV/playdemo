package models.accounts;

import javax.persistence.*;
import java.math.BigDecimal;

import models.accounts.util.SerialUtil;
import play.db.jpa.Model;

import java.util.*;

@Entity
@Table(name="payment_request")
public class PaymentBill extends Model {
    @ManyToOne
    @JoinColumn(name = "payment_source")
    public PaymentSource paymentSource;

    @Column(name = "serial_number")
    public String serialNumber;

    public BigDecimal amount;

    @Column(name = "created_at")
    public Date createdAt;

    public PaymentBill(BigDecimal amount, PaymentSource paymentSource){
        this.amount = amount;
        this.paymentSource = paymentSource;
        this.createdAt = new Date();
        this.serialNumber = SerialUtil.generateSerialNumber(this.createdAt);
    }

    public static PaymentBill make(BigDecimal amount, PaymentSource paymentSource){
        PaymentBill bill = new PaymentBill(amount, paymentSource);

        return bill;
    }
}

