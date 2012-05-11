package models.accounts;

import javax.persistence.*;
import java.math.BigDecimal;

import models.accounts.util.SerialNumberUtil;
import play.db.jpa.Model;

import java.util.*;

/**
 * 交易流水
 *
 * User:likang
 */
@Entity
@Table(name="trade_bill")
public class TradeBill extends Model {

    @Column(name = "serial_number")
    public String serialNumber;             //流水号

    public BigDecimal amount;               //总金额

    @ManyToOne
    @JoinColumn(name = "from_account")
    public Account fromAccount;             //付款方账户

    @ManyToOne
    @JoinColumn(name = "to_account")
    public Account toAccount;               //收款方账户，默认为平台收款账户

    @Column(name = "order_id")
    public Long orderId;                    //订单编号，仅当付款对象是订单时有效
    
    @Column(name = "ecoupon_sn")
    public String eCouponSn;				//电子券券号

    @Column(name = "balance_payment_amount")
    public BigDecimal balancePaymentAmount; //余额支付金额

    @Column(name = "ebank_payment_amount")
    public BigDecimal ebankPaymentAmount;   //网银支付金额

    @ManyToOne
    @JoinColumn(name = "payment_source")
    public PaymentSource paymentSource;     //网银渠道

    @Column(name = "create_at")
    public Date createdAt;                  //创建时间

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type")
    public TradeType tradeType;             //交易类型

    @Column(name = "return_code")
    public String returnCode;               //返回码

    @Column(name = "return_note")
    public String returnNote;               //返回说明

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    public TradeStatus tradeStatus;
    
    public TradeBill(){
    	this.amount = BigDecimal.ZERO;
    	this.balancePaymentAmount = BigDecimal.ZERO;
    	this.ebankPaymentAmount = BigDecimal.ZERO;
    	this.paymentSource = null;
    	this.tradeType = null;
    	this.tradeStatus = TradeStatus.UNPAID;
    	
    	this.fromAccount = null;
    	this.toAccount = null;
    	
    	this.orderId = null;
    	this.eCouponSn = null;
    	
    	
        this.createdAt = new Date();
        this.serialNumber = SerialNumberUtil.generateSerialNumber(this.createdAt);

        this.returnCode = null;
        this.returnNote = null;
    }

    public TradeBill(Account fromAccount,Account toAccount, BigDecimal balancePaymentAmount,
                     BigDecimal ebankPaymentAmount, TradeType tradeType, PaymentSource paymentSource, Long orderId){
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;

        if(balancePaymentAmount == null){
            this.balancePaymentAmount = BigDecimal.ZERO;
        }else {
            this.balancePaymentAmount = balancePaymentAmount;
        }

        this.ebankPaymentAmount = ebankPaymentAmount;
        if(ebankPaymentAmount == null){
            this.ebankPaymentAmount = BigDecimal.ZERO;
        }else {
            this.ebankPaymentAmount = ebankPaymentAmount;
        }

        this.amount = this.balancePaymentAmount.add(this.ebankPaymentAmount);

        this.tradeType = tradeType;
        this.tradeStatus = TradeStatus.UNPAID;
        this.paymentSource = paymentSource;
        this.orderId = orderId;

        this.createdAt = new Date();
        this.serialNumber = SerialNumberUtil.generateSerialNumber(this.createdAt);

        this.returnCode = null;
        this.returnNote = null;
    }



}

