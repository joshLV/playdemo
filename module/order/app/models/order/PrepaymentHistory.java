package models.order;

import com.uhuila.common.constants.DeletedStatus;
import models.accounts.SettlementStatus;
import models.supplier.Supplier;
import play.data.validation.Max;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.view_ext.annotation.Money;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * User: yan
 * Date: 13-6-20
 * Time: 下午7:02
 */
@Entity
@Table(name = "prepayment_histories")
public class PrepaymentHistory extends Model {
    @Column(name="prepayment_id")
    public Long prepaymentId;   //预付款Id或WithdrawBill

    @ManyToOne
    public Supplier supplier;   //商户

    public BigDecimal amount;   //金额

    @Column(name = "withdraw_amount")
    public BigDecimal withdrawAmount = BigDecimal.ZERO;   //已结算金额

    @Column(name = "effective_at")
    public Date effectiveAt;    //有效期开始时间

    @Column(name = "expire_at")
    public Date expireAt;       //有效期结束时间

    public String remark;       //备注

    @Column(name = "created_at")
    public Date createdAt;      //创建时间

    @Column(name = "created_by")
    public String createdBy;    //创建人帐号

    @Column(name = "settlement_status")
    @Enumerated(EnumType.STRING)
    public SettlementStatus settlementStatus = SettlementStatus.UNCLEARED;   //结算状态

}
