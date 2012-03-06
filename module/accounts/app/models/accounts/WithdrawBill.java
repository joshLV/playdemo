package models.accounts;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author likang
 * Date: 12-3-6
 */
@Entity
@Table
public class WithdrawBill extends Model {
    
    public String serialNumber;         
    
    @ManyToOne
    public Account account;
    
    public BigDecimal amount;           //退款金额
    
    @Column(name = "order_id")
    public Long orderId;                //订单号
    
    @Column(name = "apply_note")
    public String applyNote;            //申请说明
    
    @Column(name = "reply_note")
    public String  replyNote;           //官方对于此次退款的说明

    @Column(name = "bank_code")
    public String bankCode;             //银行代码
    
    @Column(name = "bank_card_number")
    public String bankCardNumber;       //银行卡卡号
    
    public String bankCardName;         //持卡人姓名

    public Date createdAt;              //创建时间

    public WithdrawBillStatus  status;

}
