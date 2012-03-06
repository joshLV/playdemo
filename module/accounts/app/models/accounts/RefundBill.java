package models.accounts;


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
    
    public RefundBill(TradeBill tradeBill, Long orderId, String applyNote, String remark, BigDecimal cashAmount,
                      BigDecimal uncashAmount){

        this.account = tradeBill.fromAccount;
        this.tradeBill = tradeBill;
        this.orderId = orderId;
        this.applyNote = applyNote;
        this.remark = remark;
        this.cashAmount = cashAmount;
        this.uncashAmount = uncashAmount;
        this.amount = this.cashAmount.add(this.uncashAmount);
        this.createdAt = new Date();
        this.serialNumber = SerialNumberUtil.generateSerialNumber(this.createdAt);
        this.refundStatus = RefundStatus.APPLIED;
    }

    public static RefundBill success(RefundBill refundBill){
        refundBill.refundStatus = RefundStatus.SUCCESS;
        refundBill.save();

        Account account = Account.findById(refundBill.account.getId());


        //保存账户变动信息
        AccountSequence accountSequence = new AccountSequence(
                account,AccountSequenceFlag.VOSTRO,                   //账务变动方向：来帐
                AccountSequenceType.REFUND,                           //变动类型：退款
                account.amount,                                       //变动前资金
                account.amount.add(refundBill.amount),                //变动后资金
                refundBill.amount,                                    //可提现金额
                BigDecimal.ZERO,                                      //不可提现金额
                refundBill.getId());                                  //相关流水号
        accountSequence.save();                                       //保存账户变动信息

        //更新账户余额
        account.addCash(refundBill.amount);
        account.save();
        refundBill.account = account;

        //保存凭证明细
        CertificateDetail certificateDetail = new CertificateDetail(
                CertificateType.DEBIT,      //借
                refundBill.amount,
                refundBill.getId(),
                "账户退款"
        );
        certificateDetail.save();

        //保存两个科目明细
        SubjectDetail subjectDetailA = new SubjectDetail(
                SubjectType.DEBIT,         //借
                certificateDetail,
                refundBill.amount,
                BigDecimal.ZERO,
                "账户退款"
        );
        subjectDetailA.save();

        SubjectDetail subjectDetailB = new SubjectDetail(
                SubjectType.CREDIT,         //贷
                certificateDetail,
                BigDecimal.ZERO,
                refundBill.amount,
                "账户退款"
        );
        subjectDetailB.save();


        return refundBill;
    }

}
