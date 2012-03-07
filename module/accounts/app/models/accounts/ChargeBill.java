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

    public ChargeBill(Account account, ChargeType chargeType, BigDecimal amount, TradeBill tradeBill){

        this.account = account;
        this.chargeType = chargeType;
        this.amount = amount;

        this.createdAt = new Date();
        this.serialNumber = SerialNumberUtil.generateSerialNumber(this.createdAt);
        this.chargeStatus = ChargeStatus.UNCHARGED;
        this.tradeBill = tradeBill;
    }

}
