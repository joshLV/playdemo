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
    public Boolean createdAt;   //是否查询日期为空
    public Account account;
    public AccountType accountType;
    public TradeType tradeType;    //资金变动类型
    public WithdrawBillStatus withdrawBillStatus;

    public Map<String, Object> params = new HashMap<>();

    /*
        期初未提现金额 (采购成本)
     */
    public String getFilterUnwithdrawedPurchaseCostAmountAmount() {
        StringBuilder filter = new StringBuilder(" where a.account.accountType = :accountType and a.tradeType = :tradeType ");
        params.put("accountType", AccountType.SUPPLIER);
        params.put("tradeType", TradeType.PURCHASE_COSTING);
        /*
            当没有选日期查询条件时，默认按最近7天之前统计
         */
        System.out.println(createdAt + "===createdAt>>");
        if (!createdAt) {
            filter.append(" and a.createdAt <= :createdAtBegin");
            params.put("createdAtBegin", com.uhuila.common.util.DateUtil.getEndOfDay(com.uhuila.common.util.DateUtil.getBeforeDate(new Date(), 8)));
        }
        System.out.println(withdrawBillStatus + "===withdrawBillStatus>>");
        if (withdrawBillStatus != null) {
            filter.append(" and a.account.id in (select w.account.id from WithdrawBill w where w.account=a.account and w.withdrawBillStatus= :withdrawBillStatus) ");
            params.put("withdrawBillStatus", withdrawBillStatus);
        }
        System.out.println(accountUid + "===accountUid>>");
        if (accountUid != null && accountUid != 0) {
            filter.append(" and a.account.uid = :accountUid");
            params.put("accountUid", accountUid);
        }
        System.out.println(createdAtBegin + "===createdAtBegin>>");
        if (createdAtBegin != null) {
            filter.append(" and a.createdAt >= :createdAtBegin");
            params.put("createdAtBegin", createdAtBegin);
        }
        System.out.println(createdAtEnd + "===createdAtEnd>>");
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
