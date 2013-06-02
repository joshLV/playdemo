package models.webop;

import com.uhuila.common.util.DateUtil;
import models.accounts.Account;
import models.accounts.AccountSequenceCondition;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.operator.Operator;
import play.db.jpa.JPA;
import utils.CrossTableConverter;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-8-7
 */
public class WithdrawReport {

    public Date reportDate;
    public AccountType accountType;
    public BigDecimal amount;

    public WithdrawReport(Date reportDate, AccountType accountType, BigDecimal amount) {
        this.reportDate = reportDate;
        this.accountType = accountType;
        this.amount = amount;
    }

    /**
     * 查询外链汇总报表.
     * @param condition
     * @return
     */
    public static List<WithdrawReport> queryWithdrawReport(AccountSequenceCondition condition) {
        Query query = JPA.em()
                .createQuery(
                        "select new models.webop.WithdrawReport(cast(s.createdAt as date), s.account.accountType, sum(s.changeAmount)) "
                                + " from AccountSequence s where "
                                + processFilter(condition) + " group by cast(s.createdAt as date),s.account.accountType order by cast(s.createdAt as date) DESC");
        for (Map.Entry<String, Object> param : condition.getParams().entrySet()) {
            query.setParameter(param.getKey(), param.getValue());
        }
        return query.getResultList();
    }

    public static String processFilter(AccountSequenceCondition condition){
        Account platformWithdrawAccount = AccountUtil.getPlatformWithdrawAccount(Operator.defaultOperator());
        StringBuilder filter = new StringBuilder("s.account != :pAccount ");
        condition.params.put("pAccount", platformWithdrawAccount);

        if (condition.createdAtBegin != null) {
            filter.append(" and s.createdAt >= :createdAtBegin");
            condition.params.put("createdAtBegin", condition.createdAtBegin);
        }
        if (condition.createdAtEnd != null) {
            filter.append(" and s.createdAt <= :createdAtEnd");
            condition.params.put("createdAtEnd", DateUtil.getEndOfDay(condition.createdAtEnd));
        }

        if (condition.tradeType != null) {
            filter.append(" and s.tradeType = :tradeType");
            condition.params.put("tradeType", condition.tradeType);
        }
        return filter.toString();
    }

    public static CrossTableConverter<WithdrawReport, BigDecimal> converter = new CrossTableConverter<WithdrawReport, BigDecimal>() {
        @Override
        public String getRowKey(WithdrawReport target) {
            return new SimpleDateFormat("yyyy-MM-dd").format(target.reportDate);
        }

        @Override
        public String getColumnKey(WithdrawReport target) {
            return target.accountType.toString();
        }

        @Override
        public BigDecimal addValue(WithdrawReport target, BigDecimal oldValue) {
            if (target.amount == null) {
                return oldValue;
            }
            if (oldValue != null) {
                return oldValue.add(target.amount);
            }
            return target.amount;
        }
    };



}
