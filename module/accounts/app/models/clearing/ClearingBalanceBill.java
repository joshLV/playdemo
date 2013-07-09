package models.clearing;

import models.accounts.Account;
import models.accounts.TradeBill;
import models.operator.OperateUser;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 结算平账记录。
 * 这里记录的是一个申请，用于调整平躺不正确的账户记录.
 * 需要审批，审批通过后，生成对应的TradeBill记录用于平账。
 */
@Entity
@Table(name = "clearing_balance_bill")
public class ClearingBalanceBill extends Model {

    /**
     * 来源账号（出款）.
     */
    @ManyToOne
    @JoinColumn(name = "from_account_id")
    public Account fromAccount;

    /**
     * 目标账号（入款）.
     */
    @ManyToOne
    @JoinColumn(name = "to_account_id")
    public Account toAccount;


    /**
     * 变动资金，应为大于0的正数.
     */
    public BigDecimal amount;

    /**
     * 原因.
     */
    public String reason;

    @Column(name = "created_at")
    public Date createdAt;

    /**
     * 建立者.
     */
    @Column(name = "created_by")
    public String createdBy;

    /**
     * 审核通过.
     */
    @Column(name = "approved_by_id")
    public OperateUser approvedBy;

    /**
     * 对应的结算交易.
     */
    @ManyToOne
    @JoinColumn(name = "trade_bill_id")
    public TradeBill tradeBill;

    /**
     * 对应为哪一笔交易冲正
     */
    @ManyToOne
    @JoinColumn(name = "target_trade_bill_id")
    public TradeBill targetTradeBill;


}
