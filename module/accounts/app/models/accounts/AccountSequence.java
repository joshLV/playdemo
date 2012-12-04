package models.accounts;

import com.uhuila.common.util.DateUtil;
import models.accounts.util.SerialNumberUtil;
import models.order.Prepayment;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 账户资金变动流水
 *
 * @author likang
 */
@Entity
@Table(name = "account_sequence")
public class AccountSequence extends Model {

    @Column(name = "serial_number")
    public String serialNumber;

    @ManyToOne
    public Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "sequence_flag")
    public AccountSequenceFlag sequenceFlag;    //账务变动方向：来账，往账

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type")
    public TradeType tradeType;

    public BigDecimal balance;                  //变动后可提现余额与不可提现余额

    @Column(name = "cash_balance")
    public BigDecimal cashBalance;              //变动后可提现余额

    @Column(name = "uncash_balance")
    public BigDecimal uncashBalance;            //变动后不可提现余额

    @Column(name = "promotion_balance")
    public BigDecimal promotionBalance;         //变动后活动金余额


    @Column(name = "change_amount")
    public BigDecimal changeAmount;             //可提现余额（包括账户余额和因提现而冻结的余额）变动发生额

    @Column(name = "promotion_change_amount")
    public BigDecimal promotionChangeAmount;    //不可提现余额发生额（活动金余额）

    @Column(name = "trade_id")
    public Long tradeId;                        //关联交易流水ID

    @Column(name = "order_id")
    public Long orderId;                        //冗余订单ID

    @Column(name = "created_at")
    public Date createdAt;                      //创建时间

    public String remark;                       //备注

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status")
    public SettlementStatus settlementStatus = SettlementStatus.UNCLEARED;   //结算状态

    @ManyToOne
    public WithdrawBill withdrawBill;

    @ManyToOne
    public Prepayment prepayment;

    @Transient
    public String orderNumber;                  //订单号

    @Transient
    public String accountName;                  //账号名称

    @Transient
    public String payMethod;                    //订单支付方式

    @Transient
    public String supplierName;                 //商户名称

    @Transient
    public String platform;                     //平台


    public AccountSequence() {

    }

    /**
     * 账户变动记录
     *
     * @param account       变动账户
     * @param sequenceFlag  入账或者出账
     * @param tradeType     交易类型
     * @param changeAmount  变动总金额
     * @param cashBalance   变动后账户可提现余额
     * @param uncashBalance 变动后账户不可提现余额
     * @param tradeId       交易ID
     */
    public AccountSequence(Account account, AccountSequenceFlag sequenceFlag, TradeType tradeType,
                           BigDecimal changeAmount, BigDecimal promotionChangeAmount,
                           BigDecimal cashBalance, BigDecimal uncashBalance, BigDecimal promotionBalance, long tradeId) {

        this.account = account;
        this.sequenceFlag = sequenceFlag;
        this.tradeType = tradeType;
        this.changeAmount = changeAmount;
        this.promotionChangeAmount = promotionChangeAmount;
        this.balance = cashBalance.add(uncashBalance);
        this.cashBalance = cashBalance;
        this.uncashBalance = uncashBalance;
        this.promotionBalance = promotionBalance;
        this.tradeId = tradeId;

        this.createdAt = new Date();
        this.serialNumber = SerialNumberUtil.generateSerialNumber(this.createdAt);
        this.remark = null;
    }

    public static JPAExtPaginator<AccountSequence> findByCondition(AccountSequenceCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<AccountSequence> page = new JPAExtPaginator<>(null, null, AccountSequence.class, condition.getFilter(),
                condition.getParams());

        page.orderBy("createdAt DESC, id DESC");
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }

    public static AccountSequenceSummary findSummaryByCondition(AccountSequenceCondition condition) {
        EntityManager entityManager = JPA.em();
        Query query = entityManager.createQuery("SELECT a.sequenceFlag, count(a.sequenceFlag), " +
                "sum(a.changeAmount) FROM AccountSequence a  WHERE " + condition.getFilter() + " group by a.sequenceFlag");
        for (String key : condition.getParams().keySet()) {
            query.setParameter(key, condition.getParams().get(key));
        }
        List<Object[]> result = query.getResultList();
        return new AccountSequenceSummary(result);
    }

    public static Map<AccountSequenceFlag, Object[]> summaryReport(Account account) {
        EntityManager entityManager = JPA.em();

        Query query = entityManager.createQuery("SELECT a.sequenceFlag, count(a.sequenceFlag), sum(a.changeAmount) " +
                "FROM AccountSequence a  WHERE a.account = :account group by a.sequenceFlag");
        query.setParameter("account", account);
        List<Object[]> list = query.getResultList();

        Map<AccountSequenceFlag, Object[]> result = new HashMap<>();
        for (Object[] ls : list) {
            result.put((AccountSequenceFlag) ls[0], ls);
        }
        if (result.get(AccountSequenceFlag.VOSTRO) == null) {
            result.put(AccountSequenceFlag.VOSTRO, new Object[]{AccountSequenceFlag.VOSTRO, 0, 0});
        }
        if (result.get(AccountSequenceFlag.NOSTRO) == null) {
            result.put(AccountSequenceFlag.NOSTRO, new Object[]{AccountSequenceFlag.NOSTRO, 0, 0});
        }

        return result;
    }

    public static BigDecimal getIncomeAmount(Account account, Date lastDate) {
        AccountSequence accountSequence = (AccountSequence) find("account=? and createdAt<? order by createdAt DESC", account, lastDate).first();

        return (accountSequence != null &&
                accountSequence.settlementStatus == SettlementStatus.UNCLEARED) ?
                accountSequence.balance : BigDecimal.ZERO;
    }

    public static BigDecimal getTodayWithdrawAmount(Account account) {
        BigDecimal amount = (BigDecimal) find("select sum(changeAmount) from AccountSequence where" +
                " account=? and sequenceFlag=? and createdAt>=? and createdAt<? group by createdAt",
                account, AccountSequenceFlag.NOSTRO, DateUtil.getBeginOfDay(), DateUtil.getEndOfDay(new Date())).first();

        return amount != null ? amount.abs() : BigDecimal.ZERO;
    }


    public static int withdraw(Account supplierAccount, Date withdrawDate, WithdrawBill withdrawBill) {
        return withdraw(supplierAccount, withdrawDate, withdrawBill, null);
    }

    /**
     * 把指定商户的所有指定日期之前的收入金额结算掉,返回update 的记录数
     *
     * @param supplierAccount 商户的账户
     * @param withdrawDate    结算截止时间
     * @param withdrawBill    结算账单
     * @return
     */
    public static int withdraw(Account supplierAccount, Date withdrawDate, WithdrawBill withdrawBill, Prepayment prepayment) {
        if (supplierAccount == null && prepayment == null) {
            return 0;
        }
        EntityManager entityManager = JPA.em();
        // 把指定商户的所有指定日期之前的收入金额结算状态改为已结算
        String prepaymentCond = prepayment == null ? "" : ",s.prepayment=:prepayment";
        String accountCond = supplierAccount == null ? "" : " s.account=:account and ";
        Query query = entityManager.createQuery("update AccountSequence as s set s.settlementStatus=:settlementStatus, s.withdrawBill = :withdrawBill " +
                prepaymentCond +
                " where " + accountCond + "s.createdAt<=:withdrawDate");
        query.setParameter("settlementStatus", SettlementStatus.CLEARED);
        query.setParameter("withdrawBill", withdrawBill);
        if (supplierAccount != null) {
            query.setParameter("account", supplierAccount);
        }
        query.setParameter("withdrawDate", DateUtil.getEndOfDay(withdrawDate));
        if (prepayment != null) {
            query.setParameter("prepayment", prepayment);
        }
        //update 的记录数
        return query.executeUpdate();
    }

    /**
     * 获取指定预付款项的已结算流水的记录数
     *
     * @return
     */
    public static long countByPrepayment(Long prepaymentId) {
        return count("prepayment.id = ?", prepaymentId);
    }
}
