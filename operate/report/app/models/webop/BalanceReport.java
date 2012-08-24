package models.webop;

import com.uhuila.common.util.DateUtil;
import models.accounts.Account;
import models.accounts.AccountSequenceCondition;
import models.accounts.TradeType;
import models.accounts.util.AccountUtil;
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
public class BalanceReport {

    public Date reportDate;
    public TradeType tradeType;
    public BigDecimal[] amounts;//amounts[0]:changeAmount; amounts[1]:promotionChangeAmount;

    public BalanceReport(Date reportDate, TradeType tradeType, BigDecimal changeAmount, BigDecimal promotionChangeAmount) {
        this.reportDate = reportDate;
        this.tradeType = tradeType;
        this.amounts = new BigDecimal[2];
        this.amounts[0] = changeAmount;
        this.amounts[1] = promotionChangeAmount;
    }

    /**
     * 查询外链汇总报表.
     * @param condition
     * @return
     */
    public static List<BalanceReport> queryWithdrawReport(AccountSequenceCondition condition) {
        Query query = JPA.em()
                .createQuery(
                        "select new models.webop.BalanceReport(cast(s.createdAt as date), s.tradeType, sum(s.changeAmount), sum(s.promotionChangeAmount)) "
                                + " from AccountSequence s where "
                                + processFilter(condition) + " group by cast(s.createdAt as date),s.tradeType order by cast(s.createdAt as date) DESC");
        for (Map.Entry<String, Object> param : condition.getParams().entrySet()) {
            query.setParameter(param.getKey(), param.getValue());
        }
        return query.getResultList();
    }

    public static String processFilter(AccountSequenceCondition condition){
        StringBuilder filter = new StringBuilder("1=1");

        if (condition.createdAtBegin != null) {
            filter.append(" and s.createdAt >= :createdAtBegin");
            condition.params.put("createdAtBegin", DateUtil.getBeginOfDay(condition.createdAtBegin));
        }
        if (condition.createdAtEnd != null) {
            filter.append(" and s.createdAt <= :createdAtEnd");
            condition.params.put("createdAtEnd", DateUtil.getEndOfDay(condition.createdAtEnd));
        }

        if (condition.accountTypes != null && condition.accountTypes.size() > 0) {
            filter.append(" and s.account.accountType in :accountTypes");
            condition.params.put("accountTypes", condition.accountTypes);
        }
        return filter.toString();
    }

    public static CrossTableConverter<BalanceReport, BigDecimal[]> converter = new CrossTableConverter<BalanceReport, BigDecimal[]>() {
        @Override
        public String getRowKey(BalanceReport target) {
            return new SimpleDateFormat("yyyy-MM-dd").format(target.reportDate);
        }

        @Override
        public String getColumnKey(BalanceReport target) {
            return target.tradeType.toString();
        }

        @Override
        public BigDecimal[] addValue(BalanceReport target, BigDecimal[] oldValue) {
            if (target.amounts == null) {
                return oldValue;
            }
            if (oldValue != null) {
                target.amounts[0] = target.amounts[0].add(oldValue[0]);
                target.amounts[1] = target.amounts[1].add(oldValue[1]);
            }
            return target.amounts;
        }
    };



}
