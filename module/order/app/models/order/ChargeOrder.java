package models.order;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import models.accounts.AccountType;

import play.db.jpa.Model;

@Entity
@Table(name = "charge_order")
public class ChargeOrder extends Model{
    @Column(name="user_id")
    public Long userId;
    @Column(name="account_type")
    public AccountType accountType;
    @Column(name = "trade_id")
    public Long tradeId;
    @Column(name = "charge_amount")
    public BigDecimal chargeAmount;
    @Column(name = "created_at")
    public Date createdAt;
    @Column(name = "paid_at")
    public Date paidAt;
    @Column(name = "payment_source")
    public String paymentSource;
    
    public ChargeOrderStatus status;
    
    @Column(name = "serial_number")
    public String serialNumber;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmssSSS");
    private static DecimalFormat decimalFormat = new DecimalFormat("00000");

    public ChargeOrder(Long userId, AccountType accountType, BigDecimal chargeAmount){
        this.userId = userId;
        this.accountType = accountType;
        this.chargeAmount = chargeAmount;
        this.createdAt = new Date();
        this.paidAt = null;
        this.serialNumber = serialNumberGenerator(this.createdAt);
        this.status = ChargeOrderStatus.UNPAID;
        this.tradeId = 0L;
    }
    
    private String serialNumberGenerator(Date date){
        int random = new Random().nextInt(10000);
        return "charge_" + dateFormat.format(date) + decimalFormat.format(random);
    }
}
