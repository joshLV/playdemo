package models.accounts;

import models.accounts.util.SerialNumberUtil;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 提现流水
 *
 * @author likang
 * Date: 12-3-6
 */
@Entity
@Table
public class WithdrawBill extends Model {
    
    public String serialNumber;         
    
    @ManyToOne
    public Account account;
    
    public BigDecimal amount;           //提现金额

    public BigDecimal fee;              //手续费
    
    @Column(name = "order_id")
    public Long orderId;                //订单号

    @Column(name = "bank_code")
    public String bankCode;             //银行代码
    
    @Column(name = "bank_card_number")
    public String bankCardNumber;       //银行卡卡号
    
    @Column(name = "bank_account_name")
    public String cardHolderName;      //持卡人姓名

    @Column(name = "return_at")
    public Date returnAt;               //银行返回时间

    @Column(name = "return_code")
    public String returnCode;           //银行返回码

    @Column(name = "return_note")
    public String  returnNote;          //银行返回备注

    @Column(name = "created_at")
    public Date createdAt;              //创建时间


    public WithdrawBillStatus  status;

    public WithdrawBill(Account account, BigDecimal amount, BigDecimal fee, Long orderId, String bankCode,
                        String bankCardNumber, String cardHolderName){
        this.createdAt = new Date();
        this.serialNumber = SerialNumberUtil.generateSerialNumber(this.createdAt);

        this.account = account;
        this.amount = amount;
        this.fee = fee;
        this.orderId = orderId;
        this.bankCode = bankCode;
        this.bankCardNumber = bankCardNumber;
        this.cardHolderName = cardHolderName;

        this.returnAt = null;
        this.returnCode = null;
        this.returnNote = null;
    }
    
}
