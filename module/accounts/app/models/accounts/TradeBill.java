package models.accounts;

import models.accounts.util.SerialNumberUtil;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 交易流水
 *
 * @author likang
 */
@Entity
@Table(name = "trade_bill")
public class TradeBill extends Model {

    @Column(name = "serial_number")
    public String serialNumber;             //流水号

    public BigDecimal amount = BigDecimal.ZERO;               //总金额

    @ManyToOne
    @JoinColumn(name = "from_account")
    public Account fromAccount;             //付款方账户

    @ManyToOne
    @JoinColumn(name = "to_account")
    public Account toAccount;               //收款方账户，默认为平台收款账户

    @Column(name = "order_id")
    public Long orderId;                    //订单编号，仅当付款对象是订单时有效

    @ManyToOne
    @JoinColumn(name = "withdraw_bill_id")
    public WithdrawBill withdrawBill;       //提现账单编号

    @Column(name = "ecoupon_sn")
    public String eCouponSn;                //电子券券号

    @Column(name = "balance_payment_amount")
    public BigDecimal balancePaymentAmount = BigDecimal.ZERO; //余额支付金额

    @Column(name = "ebank_payment_amount")
    public BigDecimal ebankPaymentAmount = BigDecimal.ZERO;   //网银支付金额

    @Column(name = "uncash_payment_amount")
    public BigDecimal uncashPaymentAmount = BigDecimal.ZERO;  //不可支付、不可提现余额支付金额

    @Column(name = "promotion_payment_amount")
    public BigDecimal promotionPaymentAmount = BigDecimal.ZERO;   //活动金余额支付金额

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

    public TradeBill() {
        this.amount = BigDecimal.ZERO;
        this.balancePaymentAmount = BigDecimal.ZERO;
        this.ebankPaymentAmount = BigDecimal.ZERO;
        this.uncashPaymentAmount = BigDecimal.ZERO;
        this.promotionPaymentAmount = BigDecimal.ZERO;
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
}

