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
    public Account toAccount;               //收款方账户，默认为uhuila的账户

    @Column(name = "order_id")
    public Long orderId;                    //订单编号，仅当付款对象是订单时有效

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
    public TradeType tradeType;         //交易类型

    @Column(name = "return_code")
    public String returnCode;               //返回码

    @Column(name = "return_note")
    public String returnNote;               //返回说明

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    public TradeStatus tradeStatus;

    public TradeBill(Account fromAccount,Account toAccount, BigDecimal amount, BigDecimal balancePaymentAmount,
                     BigDecimal ebankPaymentAmount, TradeType tradeType, PaymentSource paymentSource, Long orderId){
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.balancePaymentAmount = balancePaymentAmount;
        this.ebankPaymentAmount = ebankPaymentAmount;
        this.tradeType = tradeType;
        this.tradeStatus = TradeStatus.UNPAID;
        this.paymentSource = paymentSource;
        this.orderId = orderId;

        this.createdAt = new Date();
        this.serialNumber = SerialNumberUtil.generateSerialNumber(this.createdAt);

        this.returnCode = null;
        this.returnNote = null;
    }

    public static TradeBill createNormalTrade(
            Account fromAccount, BigDecimal amount, BigDecimal balancePaymentAmount,
            BigDecimal ebankPaymentAmount, TradeType tradeType, PaymentSource paymentSource, Long orderId){
        return new TradeBill(fromAccount,
                Account.getUhuilaAccount(),  //默认使用uhuila账户作为收款账户
                amount,
                balancePaymentAmount,
                ebankPaymentAmount,
                tradeType,
                paymentSource,
                orderId);
    }


    //网银支付成功
    public static TradeBill success(TradeBill tradeBill){
        //重新加载账户信息
        Account fromAccount = Account.findById(tradeBill.fromAccount.getId());
        Account toAccount = Account.findById(tradeBill.toAccount.getId());

        //余额不足以支付订单中指定的使用余额付款的金额
        //则将充值的钱打入发起人账户里
        if(tradeBill.balancePaymentAmount != null
                && fromAccount.amount.compareTo(tradeBill.balancePaymentAmount) < 0){
            tradeBill.tradeStatus = TradeStatus.FAILED;

            return tradeBill;
        }

        tradeBill.tradeStatus = TradeStatus.SUCCESS;
        tradeBill.save();

        //记录AccountSequence

        //更新账户余额

        return tradeBill;
    }

}

