package models.webop;

import com.uhuila.common.util.DateUtil;
import models.accounts.Account;
import models.accounts.AccountSequenceCondition;
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
public class PaymentReport {

    public static Account alipayAccount = AccountUtil.getPaymentPartnerAccount(AccountUtil.PARTNER_ALIPAY, Operator.defaultOperator());
    public static Account tenpayAccount = AccountUtil.getPaymentPartnerAccount(AccountUtil.PARTNER_TENPAY, Operator.defaultOperator());
    public static Account kuaiqianAccount = AccountUtil.getPaymentPartnerAccount(AccountUtil.PARTNER_99BILL, Operator.defaultOperator());

    public Date reportDate;
    public Account account;
    public BigDecimal amount;

    public PaymentReport(Date reportDate, Account account, BigDecimal amount) {
        this.reportDate = reportDate;
        this.account = account;
        this.amount = amount;
    }

    /**
     * 查询外链汇总报表.
     * @param condition
     * @return
     */
    public static List<PaymentReport> queryPaymentReport(AccountSequenceCondition condition) {
        Query query = JPA.em()
                .createQuery(
                        "select new models.webop.PaymentReport(cast(s.createdAt as date), s.account, sum(s.changeAmount)) "
                                + " from AccountSequence s where "
                                + processFilter(condition) + " group by cast(s.createdAt as date),s.account order by cast(s.createdAt as date) DESC");
        for (Map.Entry<String, Object> param : condition.getParams().entrySet()) {
            query.setParameter(param.getKey(), param.getValue());
        }
        return query.getResultList();
    }

    public static String processFilter(AccountSequenceCondition condition){
        StringBuilder filter = new StringBuilder("1=1");

        if (condition.createdAtBegin != null) {
            filter.append(" and s.createdAt >= :createdAtBegin");
            condition.params.put("createdAtBegin", condition.createdAtBegin);
        }
        if (condition.createdAtEnd != null) {
            filter.append(" and s.createdAt <= :createdAtEnd");
            condition.params.put("createdAtEnd", DateUtil.getEndOfDay(condition.createdAtEnd));
        }
        if(condition.accounts != null && condition.accounts.size() > 0) {
            filter.append(" and s.account in :accounts");
            condition.params.put("accounts", condition.accounts);
        }
        return filter.toString();
    }

    public static CrossTableConverter<PaymentReport, BigDecimal> converter = new CrossTableConverter<PaymentReport, BigDecimal>() {
        @Override
        public String getRowKey(PaymentReport target) {
            return new SimpleDateFormat("yyyy-MM-dd").format(target.reportDate);
        }

        @Override
        public String getColumnKey(PaymentReport target) {
            if(target.account.id.equals(alipayAccount.id)) {
                return "支付宝";
            }else if(target.account.id.equals(tenpayAccount.id)) {
                return "财付通";
            }else if(target.account.id.equals(kuaiqianAccount.id)) {
                return "快钱";
            }else {
                return "未知";
            }
        }

        @Override
        public BigDecimal addValue(PaymentReport target, BigDecimal oldValue) {
            if (target.amount == null) {
                return oldValue;
            }
            if (oldValue != null) {
                return oldValue.add(target.amount.negate());
            }
            return target.amount.negate();
        }
    };



}
