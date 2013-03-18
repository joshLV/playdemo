package models.accounts;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 商户提现汇总查询条件
 * <p/>
 * User: wangjia
 * Date: 13-3-18
 * Time: 上午10:52
 */
public class SupplierWithdrawCondition implements Serializable {
    public Long accountUid;
    public Date createdAtBegin;
    public Date createdAtEnd;
    public Account account;
    public AccountType accountType;
    public TradeType tradeType;    //资金变动类型
    public WithdrawBillStatus withdrawBillStatus;

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
            params.put("createdAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(createdAtEnd));
        }

        if (tradeType != null) {
            filter.append(" and tradeType = :tradeType");
            params.put("tradeType", tradeType);
        }

        if (accountUid != null) {
            filter.append(" and account.uid = :accountUid");
            params.put("accountUid", accountUid);
        }
        if (withdrawBillStatus != null) {
            filter.append(" and account.uid = :accountUid");
            params.put("accountUid", accountUid);
        }

        return filter.toString();
    }

    public Map<String, Object> getParams() {
        return params;
    }

}
