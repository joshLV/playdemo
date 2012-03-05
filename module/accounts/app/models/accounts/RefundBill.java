package models.accounts;


import models.accounts.util.SerialUtil;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * User: likang
 * Date: 12-3-5
 */
@Entity
@Table(name = "refund_bill")
public class RefundBill extends Model {

    public String serial;

    @ManyToOne
    @JoinColumn(name = "payment_bill")
    public PaymentBill paymentBill;         //原支付流水

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
    
    public RefundBill(PaymentBill paymentBill, Long orderId, String applyNote, String remark, BigDecimal cashAmount,
                      BigDecimal uncashAmount){


        this.paymentBill = paymentBill;
        this.orderId = orderId;
        this.applyNote = applyNote;
        this.remark = remark;
        this.cashAmount = cashAmount;
        this.uncashAmount = uncashAmount;
        this.amount = this.cashAmount.add(this.uncashAmount);
        this.createdAt = new Date();
        this.serial = SerialUtil.generateSerialNumber(this.createdAt);
        this.refundStatus = RefundStatus.APPLIED;
    }

}
