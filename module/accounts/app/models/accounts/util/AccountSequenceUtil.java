package models.accounts.util;

import com.uhuila.common.util.DateUtil;
import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.TradeType;
import models.accounts.WithdrawBill;
import org.apache.commons.collections.CollectionUtils;
import play.db.jpa.JPA;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 账户资金变动的工具类.
 * <p/>
 * User: sujie
 * Date: 1/11/13
 * Time: 10:19 AM
 */
public class AccountSequenceUtil {

    /**
     * 检查每笔流水的余额是否正确.
     *
     * @param account
     * @return 出错的AccountSequence
     */
    public static AccountSequence checkBalance(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("accountId can not be null");
        }
        List<AccountSequence> accountSequences = AccountSequence.find("account=? order by id", account).fetch();
        final Date beforeDate = DateUtil.stringToDate("20120529", "yyyyMMdd");

        BigDecimal lastBalance = BigDecimal.ZERO;
        int i = 0;
        for (AccountSequence accountSequence : accountSequences) {
            BigDecimal correctBalance = lastBalance.add(accountSequence.changeAmount);
            if (correctBalance.compareTo(accountSequence.balance) != 0) {
                if (i++ == 0 && account.createdAt.before(beforeDate)) {
                    System.out.println(new Date() + "  Default balance account id:" + account.id + ",uid:" + account.uid + ",balance=" + accountSequence.balance);
                } else {
                    return accountSequence;
                }
            }
            lastBalance = accountSequence.balance;
        }

        return null;
    }

    /**
     * 检查每笔流水的promotion_balance是否正确.
     *
     * @param accountId
     * @return
     */
    public static AccountSequence checkPromotionBalance(Long accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("accountId can not be null");
        }
        BigDecimal lastBalance = BigDecimal.ZERO;
        List<AccountSequence> accountSequenceList = AccountSequence.find("promotionChangeAmount!=0 and account.id=? order by id", accountId).fetch();
        for (AccountSequence accountSequence : accountSequenceList) {
            BigDecimal correctBalance = lastBalance.add(accountSequence.promotionChangeAmount);
            if (correctBalance.compareTo(accountSequence.promotionBalance) != 0) {
                System.out.println("correctBalance:" + correctBalance);
                System.out.println("accountSequence.promotionBalance:" + accountSequence.promotionBalance);
                return accountSequence;
            }
            lastBalance = accountSequence.promotionBalance;
        }
        return null;
    }


    public enum MismatchBalance {BALANCE, CASH_BALANCE, UNCASH_BALANCE, PROMOTION_BALANCE}

    /**
     * 检查账户余额、不可提现余额、活动金余额和account_sequence的最后记录的余额、不可提现余额、活动金余额是否一致.
     *
     * @param accountId 被检测的账户id
     * @return 返回余额是否一致
     */
    public static List<MismatchBalance> checkAccountAmount(Long accountId) {
        List<MismatchBalance> mismatchBalanceList = new ArrayList<>();
        Account account = Account.findById(accountId);
        AccountSequence lastAccountSeq = AccountSequence.getLastAccountSequence(accountId);
        if (lastAccountSeq == null) {
            if (account.amount.compareTo(BigDecimal.ZERO) != 0) {
                mismatchBalanceList.add(MismatchBalance.CASH_BALANCE);
            }
            if (account.uncashAmount != null && account.uncashAmount.compareTo(BigDecimal.ZERO) != 0) {
                mismatchBalanceList.add(MismatchBalance.UNCASH_BALANCE);
            }
            if (account.promotionAmount != null && account.promotionAmount.compareTo(BigDecimal.ZERO) != 0) {
                mismatchBalanceList.add(MismatchBalance.PROMOTION_BALANCE);
            }
            return mismatchBalanceList;
        }
        //正在申请中的不可提现总金额
        BigDecimal applyingUncashAmount = WithdrawBill.getApplyingAmountFrom(account, lastAccountSeq.createdAt);

        if (lastAccountSeq.uncashBalance != null && lastAccountSeq.uncashBalance.add(applyingUncashAmount).compareTo(account.uncashAmount) != 0) {
            mismatchBalanceList.add(MismatchBalance.UNCASH_BALANCE);
        }
        if (lastAccountSeq.cashBalance.subtract(applyingUncashAmount).compareTo(account.amount) != 0) {
            mismatchBalanceList.add(MismatchBalance.CASH_BALANCE);
        }
        if (lastAccountSeq.balance.compareTo(account.amount.add(account.uncashAmount)) != 0) {
            mismatchBalanceList.add(MismatchBalance.BALANCE);
        }
        if (lastAccountSeq.promotionBalance != null && account.promotionAmount != null && lastAccountSeq.promotionBalance.compareTo(account.promotionAmount) != 0) {
            mismatchBalanceList.add(MismatchBalance.PROMOTION_BALANCE);
        }
        return mismatchBalanceList;
    }

    /**
     * 检查帐号余额守恒状态
     * account_sequence的balance=cashBalance+uncashBalance
     *
     * @return 是否通过检查
     */
    public static long checkBalanceConservation() {
        return AccountSequence.count("balance!=cashBalance+uncashBalance");
    }

    public static boolean checkTradeBalance(Account account, AccountSequence lastAccountSequence) {

        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("SELECT SUM(ebankPaymentAmount+uncashPaymentAmount+balancePaymentAmount) FROM TradeBill " +
                "WHERE  toAccount =:account and createdAt<=:createdAt");
        q.setParameter("account", account);
        q.setParameter("createdAt", lastAccountSequence.createdAt);
        BigDecimal incomeAmount = q.getSingleResult() == null ? BigDecimal.ZERO : (BigDecimal) q.getSingleResult();
        q = entityManager.createQuery("SELECT SUM(ebankPaymentAmount+uncashPaymentAmount+balancePaymentAmount) FROM TradeBill " +
                "WHERE  fromAccount =:account and createdAt<=:createdAt");
        q.setParameter("account", account);
        q.setParameter("createdAt", lastAccountSequence.createdAt);

        BigDecimal expendAmount = q.getSingleResult() == null ? BigDecimal.ZERO : (BigDecimal) q.getSingleResult();
        BigDecimal tradeBillAmount = incomeAmount.subtract(expendAmount);
        return lastAccountSequence.balance.compareTo(tradeBillAmount) == 0;
    }


    public static boolean checkTradePromotionBalance(Account account, AccountSequence lastAccountSequence) {

        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("SELECT SUM(promotionPaymentAmount) FROM TradeBill " +
                "WHERE  toAccount =:account and createdAt<=:createdAt");
        q.setParameter("account", account);
        q.setParameter("createdAt", lastAccountSequence.createdAt);
        BigDecimal incomeAmount = q.getSingleResult() == null ? BigDecimal.ZERO : (BigDecimal) q.getSingleResult();
        q = entityManager.createQuery("SELECT SUM(promotionPaymentAmount) FROM TradeBill " +
                "WHERE  fromAccount =:account and createdAt<=:createdAt");
        q.setParameter("account", account);
        q.setParameter("createdAt", lastAccountSequence.createdAt);

        BigDecimal expendAmount = q.getSingleResult() == null ? BigDecimal.ZERO : (BigDecimal) q.getSingleResult();
        BigDecimal tradeBillPromotionAmount = incomeAmount.subtract(expendAmount);
        return lastAccountSequence.promotionBalance.compareTo(tradeBillPromotionAmount) == 0;
    }

    /**
     * 根据account_sequence中的changeAmount和promotionChangeAmount
     *
     * @param account
     */
    public static int fixBalance(Account account) {
        System.out.println("check and fix account:" + account.id);
        List<AccountSequence> accountSequenceList = AccountSequence.find("account=? order by id", account).fetch();

        BigDecimal lastBalance = BigDecimal.ZERO;
        BigDecimal lastUncashBalance = BigDecimal.ZERO;
        BigDecimal lastPromotionBalance = BigDecimal.ZERO;

        if (CollectionUtils.isNotEmpty(accountSequenceList)) {
            lastBalance = accountSequenceList.get(0).balance;
            lastUncashBalance = accountSequenceList.get(0).uncashBalance;
            lastPromotionBalance = accountSequenceList.get(0).promotionBalance;
        }
        boolean isChanged = false;
        int i = 0;
        for (AccountSequence accountSequence : accountSequenceList) {
            if (i++ == 0) {
                continue;
            }
            isChanged = false;
            //必须先纠正balance，再修改uncashBalance,然后根据balance和uncashBalance算出cashBalance
            if (accountSequence.balance.compareTo(lastBalance.add(accountSequence.changeAmount)) != 0) {
                //---------------balance=cash_balance+uncash_balance
                accountSequence.balance = lastBalance.add(accountSequence.changeAmount);
                isChanged = true;
            }
            if (accountSequence.tradeType == TradeType.WITHDRAW) {
                //结算类型的流水，changeAmount是不可提现的金额
                if (accountSequence.uncashBalance.compareTo(lastUncashBalance.add(accountSequence.changeAmount)) != 0) {

                    //--------------uncash_balance = lastUncashBalance+change_amount
                    accountSequence.uncashBalance = lastUncashBalance.add(accountSequence.changeAmount);
                    isChanged = true;
                }
            }
            if (accountSequence.cashBalance.compareTo(accountSequence.balance.subtract(accountSequence.uncashBalance)) != 0) {
                //--------------cash_balance = lastCashBalance+change_amount
                accountSequence.cashBalance = accountSequence.balance.subtract(accountSequence.uncashBalance);
                isChanged = true;
            }
            if (accountSequence.promotionBalance.compareTo(lastPromotionBalance.add(accountSequence.promotionChangeAmount)) != 0) {
                accountSequence.promotionBalance = lastPromotionBalance.add(accountSequence.promotionChangeAmount);
                isChanged = true;
            }
            if (isChanged) {
                //如需实时显示执行进度，可打开下面的打印语句
                //System.out.println("----------- (" + (++i) + ") fix seq:" + accountSequence.id);
                accountSequence.save();
                if (i % 50 == 0) {
                    JPA.em().flush();
                }
            }

            lastUncashBalance = accountSequence.uncashBalance;
            lastPromotionBalance = accountSequence.promotionBalance;
            lastBalance = accountSequence.balance;
        }
        return i;
    }
}
