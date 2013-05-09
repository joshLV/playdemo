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
    public Date createdAtBegin;
    public Date createdAtEnd;
    //    public Account account;
//    public TradeType tradeType;    //资金变动类型
    public WithdrawBillStatus withdrawBillStatus;
    public Map<String, Object> params = new HashMap<>();

    public Account account;
    public AccountType accountType;

    /*
        期初未提现金额 (采购成本)
     */
    public String getFilterPurchaseCost() {
        StringBuilder filter = new StringBuilder(" where a.tradeType = :tradeType ");
        params.put("tradeType", TradeType.PURCHASE_COSTING);

        if (account != null) {
            filter.append(" and account = :account");
            params.put("account", account);
        }
        if (accountType != null) {
            filter.append(" and account.accountType = :accountType ");
            params.put("accountType", accountType);
        }

        if (withdrawBillStatus != null) {
            filter.append(" and a.account.id in (select w.account.id from WithdrawBill w where w.account=a.account and w.status= :withdrawBillStatus) ");
            params.put("withdrawBillStatus", withdrawBillStatus);
        }
        if (createdAtBegin != null) {
            filter.append(" and a.createdAt <= :createdAtBegin");
            params.put("createdAtBegin", com.uhuila.common.util.DateUtil.getEndOfDay(com.uhuila.common.util.DateUtil.getBeforeDate(createdAtBegin, 1)));
        }
        return filter.toString();
    }

    /*
       期初未提现金额 (提现)
    */
    public String getFilterPreviousWithdrawnAmount() {
        StringBuilder filter = new StringBuilder(" where a.tradeType = :tradeType ");
        params.put("tradeType", TradeType.WITHDRAW);
        if (account != null) {
            filter.append(" and account = :account");
            params.put("account", account);
        }
        if (accountType != null) {
            filter.append(" and account.accountType = :accountType ");
            params.put("accountType", accountType);
        }
        if (withdrawBillStatus != null) {
            filter.append(" and a.account.id in (select w.account.id from WithdrawBill w where w.account=a.account and w.status= :withdrawBillStatus) ");
            params.put("withdrawBillStatus", withdrawBillStatus);
        }
        if (createdAtBegin != null) {
            filter.append(" and a.createdAt <= :createdAtBegin");
            params.put("createdAtBegin", com.uhuila.common.util.DateUtil.getEndOfDay(com.uhuila.common.util.DateUtil.getBeforeDate(createdAtBegin, 1)));
        }

        return filter.toString();
    }

    /*
        本周期券消费金额
     */
    public String getFilterConsumedAmount() {
        StringBuilder filter = new StringBuilder(" where a.tradeType = :tradeType ");
        params.put("tradeType", TradeType.PURCHASE_COSTING);
        if (account != null) {
            filter.append(" and account = :account");
            params.put("account", account);
        }
        if (accountType != null) {
            filter.append(" and account.accountType = :accountType ");
            params.put("accountType", accountType);
        }
        if (withdrawBillStatus != null) {
            filter.append(" and a.account.id in (select w.account.id from WithdrawBill w where w.account=a.account and w.status= :withdrawBillStatus) ");
            params.put("withdrawBillStatus", withdrawBillStatus);
        }

        if (createdAtBegin != null) {
            filter.append(" and a.createdAt >= :createdAtBegin");
            params.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            filter.append(" and a.createdAt <= :createdAtEnd");
            params.put("createdAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(createdAtEnd));
        }

        return filter.toString();
    }


    /*
       本周期提现金额
    */
    public String getFilterWithdrawnAmount() {
        StringBuilder filter = new StringBuilder(" where a.tradeType = :tradeType ");
        params.put("tradeType", TradeType.WITHDRAW);
        if (account != null) {
            filter.append(" and account = :account");
            params.put("account", account);
        }
        if (accountType != null) {
            filter.append(" and account.accountType = :accountType ");
            params.put("accountType", accountType);
        }
        if (withdrawBillStatus != null) {
            filter.append(" and a.account.id in (select w.account.id from WithdrawBill w where w.account=a.account and w.status= :withdrawBillStatus) ");
            params.put("withdrawBillStatus", withdrawBillStatus);
        }

        if (createdAtBegin != null) {
            filter.append(" and a.createdAt >= :createdAtBegin");
            params.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            filter.append(" and a.createdAt <= :createdAtEnd");
            params.put("createdAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(createdAtEnd));
        }

        return filter.toString();
    }


    public Map<String, Object> getParams() {
        return params;
    }

}
