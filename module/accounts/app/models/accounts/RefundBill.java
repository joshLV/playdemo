package models.accounts;


import models.accounts.util.AccountUtil;
import models.accounts.util.SerialNumberUtil;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 退款交易流水
 *
 * User: likang
 * Date: 12-3-5
 */
@Entity
@Table(name = "refund_bill")
public class RefundBill extends Model {

    @Column(name = "serial_number")
    public String serialNumber;

    @ManyToOne
    public Account account;

    @ManyToOne
    @JoinColumn(name = "payment_bill")
    public TradeBill tradeBill;             //原支付流水

    @Column(name = "order_id")
    public Long orderId;                    //订单号

    @Column(name = "apply_note")
    public String applyNote;                //退款说明

    public String remark;                   //对于此退款申请的反馈

    @Column(name = "cash_amount")
    public BigDecimal cashAmount;           //可提现金额

    @Column(name = "uncash_amount")
    public BigDecimal uncashAmount;         //不可提现金额

    public BigDecimal amount;               //总金额

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status")
    public RefundStatus refundStatus;


    @Column(name = "create_at")
    public Date createdAt;
    
    public RefundBill(TradeBill tradeBill, Long orderId, String applyNote, BigDecimal cashAmount,
                      BigDecimal uncashAmount){

        this.account = tradeBill.fromAccount;
        this.tradeBill = tradeBill;
        this.orderId = orderId;
        this.applyNote = applyNote;
        this.remark = null;
        if(cashAmount == null){
            this.cashAmount = BigDecimal.ZERO;
        }else {
            this.cashAmount = cashAmount;
        }

        if(uncashAmount == null){
            this.uncashAmount = BigDecimal.ZERO;
        }else {
            this.uncashAmount = uncashAmount;
        }

        this.amount = this.cashAmount.add(this.uncashAmount);
        this.createdAt = new Date();
        this.serialNumber = SerialNumberUtil.generateSerialNumber(this.createdAt);
        this.refundStatus = RefundStatus.APPLIED;
    }
}
