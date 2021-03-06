package models.accounts;

import models.order.Prepayment;
import play.db.jpa.Model;
import play.modules.solr.SolrField;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 每天每个账户结算金额
 * <p/>
 * User: wangjia
 * Date: 13-7-5
 * Time: 下午2:40
 */
@Entity
@Table(name = "cleared_account")
public class ClearedAccount extends Model {
    @Column(name = "account_id")
    public Long accountId;

    @Column(name = "amount")
    public BigDecimal amount = BigDecimal.ZERO;

    public Date date;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "cleared_account_id")
    public List<AccountSequence> accountSequences;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status")
    public SettlementStatus settlementStatus = SettlementStatus.UNCLEARED;   //结算状态

    @ManyToOne
    @JoinColumn(name = "withdraw_bill_id")
    public WithdrawBill withdrawBill;

    /**
     * 修改时间
     */
    @Column(name = "updated_at")
    public Date updatedAt;

    /**
     * 修改人
     */
    @Column(name = "updated_by")
    @SolrField
    public String updatedBy;

    @ManyToOne
    public Prepayment prepayment;


    public static BigDecimal getClearedAmount(Account account, Date toDate) {
        BigDecimal amount = (BigDecimal) find("select sum(amount) from ClearedAccount where" +
                " accountId=? and settlementStatus=? and date < ?",
                account.id, SettlementStatus.UNCLEARED, toDate).first();
        return amount != null ? amount : BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "ClearedAccount{" +
                "accountId=" + accountId +
                ", amount=" + amount +
                ", date=" + date +
                ", accountSequences=" + accountSequences +
                ", settlementStatus=" + settlementStatus +
                ", withdrawBill=" + withdrawBill +
                ", updatedAt=" + updatedAt +
                ", updatedBy='" + updatedBy + '\'' +
                ", prepayment=" + prepayment +
                '}';
    }
}
