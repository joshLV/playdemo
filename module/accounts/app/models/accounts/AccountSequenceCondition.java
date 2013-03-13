package models.accounts;

import com.uhuila.common.util.DateUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 账户资金明细查询条件.
 * todo 目前先实现只按创建时间、资金流向、支付类型来查询
 * <p/>
 * User: sujie
 * Date: 3/7/12
 * Time: 7:10 PM
 */
public class AccountSequenceCondition implements Serializable {
    public Account account;
    public Long accountUid;
    public List<Account> accounts;
    public AccountType accountType;
    public List<AccountType> accountTypes;
    public AccountSequenceFlag sequenceFlag;    //账务变动方向：来账，往账
    public TradeType tradeType;    //资金变动类型
    public Date createdAtBegin;
    public Date createdAtEnd;
    public String interval;
    public BigDecimal changeAmount;

    //用于保存界面的查询条件
    public String accountName;

    public Map<String, Object> params = new HashMap<>();

    public String getFilter() {
        StringBuilder filter = new StringBuilder("1=1");
        if (account != null) {
            filter.append(" and account = :account");
            params.put("account", account);
        }
        if (accountType != null) {
            filter.append(" and account.accountType = :accountType ");
            params.put("accountType", accountType);
        }
        if (createdAtBegin != null) {
            filter.append(" and createdAt >= :createdAtBegin");
            params.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            filter.append(" and createdAt <= :createdAtEnd");
            params.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }
        if (sequenceFlag != null) {
            filter.append(" and sequenceFlag= :sequenceFlag");
            params.put("sequenceFlag", sequenceFlag);
        }
        if (tradeType != null) {
            filter.append(" and tradeType = :tradeType");
            params.put("tradeType", tradeType);
        }
        if (changeAmount != null) {
            filter.append(" and changeAmount = :changeAmount");
            params.put("changeAmount", changeAmount);
        }
        if (accountUid != null) {
            filter.append(" and account.uid = :accountUid");
            params.put("accountUid", accountUid);
        }
        return filter.toString();
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
