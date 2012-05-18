package models.accounts;

import models.accounts.util.SerialNumberUtil;
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
 * <p/>
 * User: likang
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
    @Column(name = "sequence_type")
    public AccountSequenceType sequenceType;    //资金变动类型

    @Column(name = "pre_amount")
    public BigDecimal preAmount;                //变动前金额

    public BigDecimal amount;                   //变动后金额

    @Column(name = "cash_amount")
    public BigDecimal cashAmount;               //可提现发生额

    @Column(name = "uncash_amount")
    public BigDecimal uncashAmount;             //不可提现发生额

    @Column(name = "trade_id")
    public Long tradeId;              //关联交易流水ID

    @Column(name = "order_id")
    public Long orderId;                        //冗余订单ID

    @Column(name = "created_at")
    public Date createdAt;                      //创建时间

    public String remark;                       //备注

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

    public AccountSequence(Account account, AccountSequenceFlag sequenceFlag, AccountSequenceType sequenceType,
                           BigDecimal preAmount, BigDecimal amount, BigDecimal cashAmount, BigDecimal uncashAmount,
                           long tradeId) {

        this.account = account;
        this.sequenceFlag = sequenceFlag;
        this.sequenceType = sequenceType;
        this.preAmount = preAmount;
        this.amount = amount;
        this.cashAmount = cashAmount;
        this.uncashAmount = uncashAmount;
        this.tradeId = tradeId;

        this.createdAt = new Date();
        this.serialNumber = SerialNumberUtil.generateSerialNumber(this.createdAt);
        this.remark = null;
    }

    public static JPAExtPaginator<AccountSequence> findByCondition(AccountSequenceCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<AccountSequence> page = new JPAExtPaginator<>(null, null, AccountSequence.class, condition.getFilter(),
                condition.getParams());

        page.orderBy("createdAt DESC");
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }

    public static AccountSequenceSummary findSummaryByCondition(AccountSequenceCondition condition) {
        EntityManager entityManager = JPA.em();
        Query query = entityManager.createQuery("SELECT a.sequenceFlag, count(a.sequenceFlag), " +
                "sum(a.amount) FROM AccountSequence a  WHERE "+condition.getFilter()+" group by a.sequenceFlag");
        for (String key : condition.getParams().keySet()) {
            query.setParameter(key,condition.getParams().get(key));
        }
        List<Object[]> result = query.getResultList();
        return new AccountSequenceSummary(result);
    }

    public static Map<AccountSequenceFlag, Object[]> summaryReport(Account account) {
        EntityManager entityManager = JPA.em();

        Query query = entityManager.createQuery("SELECT a.sequenceFlag, count(a.sequenceFlag), sum(a.amount) FROM AccountSequence a  WHERE a.account = :account group by a.sequenceFlag");
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
/*

    public static JPAExtPaginator<DailyReport> reportByDate(Date begin, Date end, int pageNumber, int pageSize) {
        EntityManager entityManager = JPA.em();

        Query query = entityManager.createQuery("SELECT count(a.sequenceFlag), sum(a.amount) FROM AccountSequence a  WHERE a.account = :account group by a.sequenceFlag");
        query.setParameter("account", account);
        List<Object[]> list = query.getResultList();
        Map<AccountSequenceFlag, Object[]> result = new HashMap<>();
        for (Object[] ls : list) {
            result.put((AccountSequenceFlag) ls[0], ls);
        }
    }
*/
//
//    public static ReportSummary reportSummaryByDate(Date begin, Date end) {
//        return null;  //To change body of created methods use File | Settings | File Templates.
//    }
}
