package models.accounts;

import models.accounts.util.SerialNumberUtil;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 充值交易流水
 *
 * User: likang
 */
@Entity
@Table(name = "charge_bill")
public class ChargeBill extends Model {

    @Column(name = "serial_number")
    public String serialNumber;

    @ManyToOne
    public Account account;                 //充值账户

    @Enumerated(EnumType.STRING)
    @Column(name = "charge_type")
    public ChargeType chargeType;           //充值类型

    public BigDecimal amount;               //充值金额

    @Column(name = "created_at")
    public Date  createdAt;                 //创建时间

    @Enumerated(EnumType.STRING)
    @Column(name = "charge_status")
    public ChargeStatus chargeStatus;       //充值状态

    @OneToOne
    @JoinColumn(name = "trade_bill")
    public TradeBill tradeBill;             //交易流水

    public ChargeBill(Account account, ChargeType chargeType,BigDecimal amount, TradeBill tradeBill){

        this.account = account;
        this.chargeType = chargeType;
        this.amount = amount;

        this.createdAt = new Date();
        this.serialNumber = SerialNumberUtil.generateSerialNumber(this.createdAt);
        this.chargeStatus = ChargeStatus.UNCHARGED;
        if(tradeBill == null){
            //自动创建TradeBill
        }else {
            this.tradeBill = tradeBill;
        }
//        this.tradeBill = new TradeBill().save();
    }

    public static ChargeBill success(ChargeBill chargeBill){
        //修改流水状态
        chargeBill.chargeStatus = ChargeStatus.SUCCESS;
        chargeBill.save();

        Account account = Account.findById(chargeBill.account.getId()); //重新加载账户信息

        //保存账户变动信息
        AccountSequence accountSequence = new AccountSequence(
                account,AccountSequenceFlag.VOSTRO,                   //账务变动方向：来帐
                AccountSequenceType.CHARGE,                           //变动类型：充值
                account.amount,                                       //变动前资金
                account.amount.add(chargeBill.amount),                //变动后资金
                chargeBill.amount,                                    //可提现金额
                BigDecimal.ZERO,                                      //不可提现金额
                chargeBill.getId());                                  //相关流水号
        accountSequence.save();                                       //保存账户变动信息

        //更新账户余额
        account.addCash(chargeBill.amount);
        account.save();
        chargeBill.account = account;

        //保存凭证明细
        CertificateDetail certificateDetail = new CertificateDetail(
                CertificateType.DEBIT,      //借
                chargeBill.amount,
                chargeBill.getId(),
                "账户充值"
        );
        certificateDetail.save();

        //保存两个科目明细
        SubjectDetail subjectDetailA = new SubjectDetail(
                SubjectType.DEBIT,         //借
                certificateDetail,
                chargeBill.amount,
                BigDecimal.ZERO,
                "账户充值"
        );
        subjectDetailA.save();

        SubjectDetail subjectDetailB = new SubjectDetail(
                SubjectType.CREDIT,         //贷
                certificateDetail,
                BigDecimal.ZERO,
                chargeBill.amount,
                "账户充值"
        );
        subjectDetailB.save();

        return chargeBill;
    }

}
