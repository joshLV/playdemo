package models.accounts;

import javax.persistence.*;
import java.math.BigDecimal;

import models.accounts.util.SerialUtil;
import play.db.jpa.Model;

import java.util.*;

/**
 * User:likang
 */
@Entity
@Table(name="payment_request")
public class PaymentBill extends Model {

    @Column(name = "serial_number")
    public String serialNumber;         //流水号

    public BigDecimal amount;           //总金额

    @ManyToOne
    @JoinColumn(name = "from_account")
    public Account fromAccount;         //付款方账户

    @ManyToOne
    @JoinColumn(name = "to_account")
    public Account toAccount;           //收款方账户，仅当付款对象是平台内账户时有效

    @Column(name = "order_id")
    public Long orderId;                //订单编号，仅当付款对象是订单时有效

    @Column(name = "balance_payment_amount")
    public BigDecimal balancePaymentAmount; //余额支付金额

    @Column(name = "ebank_payment_amount")
    public BigDecimal ebankPaymentAmount;    //网银支付金额

    @ManyToOne
    @JoinColumn(name = "payment_source")
    public PaymentSource paymentSource;     //网银渠道

    @Column(name = "payee_bank_name")
    public String payeeBankName;            //收款方银行，仅当提现时有效

    @Column(name = "payee_card_number")
    public String payeeCardNumber;      //收款方银行卡号，仅当提现时有效

    @Column(name = "payee_card_name")
    public String payeeCardName;        //收款方开户名，仅当提现时有效

    @Column(name = "expire_at")
    public Date expireAt;                   //超时未支付的截止时间

    @Column(name = "create_at")
    public Date createdAt;                   //创建时间

    @ManyToOne
    @JoinColumn(name = "payment_type")
    public PaymentType paymentType;         //交易类型

    @Column(name = "return_code")
    public String returnCode;               //返回码

    @Column(name = "return_note")
    public String returnNote;               //返回说明

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    public PaymentStatus paymentStatus;


    public PaymentBill(Account fromAccount, BigDecimal amount, BigDecimal balancePaymentAmount,
                       BigDecimal ebankPaymentAmount, PaymentType paymentType){
        this.fromAccount = fromAccount;
        this.amount = amount;
        this.balancePaymentAmount = balancePaymentAmount;
        this.ebankPaymentAmount = ebankPaymentAmount;
        this.paymentType = paymentType;
        this.paymentStatus = PaymentStatus.UNPAID;

        this.createdAt = new Date();
        this.serialNumber = SerialUtil.generateSerialNumber(this.createdAt);

        this.toAccount = null;
        this.orderId = null;
        this.paymentSource = null;
        this.payeeBankName = null;
        this.payeeCardName = null;
        this.payeeCardNumber = null;
        this.expireAt = null;
        this.returnCode = null;
        this.returnNote = null;
    }

}

