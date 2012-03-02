package models.accounts;

import models.accounts.util.SerialUtil;
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
@Table(name = "account_charge_bill")
public class AccountChargeBill extends Model {

    public String  serial;

    @ManyToOne
    public Account account;                 //充值账户

    @Enumerated
    @Column(name = "charge_type")
    public AccountChargeType chargeType;    //充值类型

    @ManyToOne
    @JoinColumn(name = "payment_source")
    public PaymentSource paymentSource;     //充值渠道

    public BigDecimal amount;               //充值金额
    
    public String remark;                   //备注

    @Column(name = "result_code")
    public String resultCode;               //返回码

    @Column(name = "result_note")
    public String resultNote;               //返回备注

    @Column(name = "created_at")
    public Date  createdAt;                 //创建时间

    @Column(name = "return_at")
    public Date  returnAt;                  //返回时间

    @Enumerated
    @Column(name = "charge_status")
    public AccountChargeStatus chargeStatus;    //充值状态

    public AccountChargeBill(Account account, AccountChargeType chargeType,PaymentSource paymentSource,
                             BigDecimal amount, String remark){
        this.serial = SerialUtil.generateSerialNumber();

        this.account = account;
        this.chargeType = chargeType;
        this.paymentSource = paymentSource;
        this.amount = amount;
        this.remark = remark;
        
        this.resultCode = null;
        this.resultNote = null;
        this.createdAt = new Date();
        this.returnAt = null;
        this.chargeStatus = AccountChargeStatus.UNCHARGED;
    }

}
