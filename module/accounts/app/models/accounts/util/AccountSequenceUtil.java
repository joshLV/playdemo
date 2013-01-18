package models.accounts.util;

import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.TradeType;
import models.accounts.WithdrawBill;
import play.db.jpa.JPA;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
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
     * 由于有些帐号有初始余额，并且初始余额来源不明缺，因此对于帐号的第一条sequence不做检查,仅做打印.
     *
     * @param account
     * @return 出错的AccountSequence
     */
    public static AccountSequence checkBalance(Account account) {
        return checkBalance(account, null);
    }

    public static AccountSequence checkBalance(Account account, Date from) {
        if (account == null) {
            throw new IllegalArgumentException("account can not be null");
        }
        List<AccountSequence> accountSequences;
        if (from == null) {
            accountSequences = AccountSequence.find("account=? order by id", account).fetch();
        } else {
            accountSequences = AccountSequence.find("account=? and createdAt>=? order by id", account, from).fetch();
        }
//        final Date beforeDate = DateUtil.stringToDate("20120529", "yyyyMMdd");

        BigDecimal lastBalance = BigDecimal.ZERO;
        BigDecimal lastPromotionBalance = BigDecimal.ZERO;
        int i = 0;
        for (AccountSequence accountSequence : accountSequences) {
            BigDecimal correctPromotionBalance = lastPromotionBalance.add(accountSequence.promotionChangeAmount);
            if (correctPromotionBalance.compareTo(accountSequence.promotionBalance) != 0) {
                System.out.println("error account promotionBalance(sequenceId:" + accountSequence.id + ",accountId:" + account.id + ",uid:" + account.uid
                        + ",accountType:" + account.accountType + ",promotionBalance:" + accountSequence.promotionBalance);
                return accountSequence;
            }
            lastPromotionBalance = accountSequence.promotionBalance;

            BigDecimal correctBalance = lastBalance.add(accountSequence.changeAmount);
            if (correctBalance.compareTo(accountSequence.balance) != 0) {
                if (i++ == 0) {
                    System.out.println(new Date() + "  Default balance account id:" + account.id + ",uid:" + account.uid
                            + ",balance=" + accountSequence.balance + ",accountCreatedAt:" + account.createdAt);
                } else {
                    System.out.println("error account balance(sequenceId:" + accountSequence.id + ",accountId:" + account.id + ",uid:" + account.uid
                            + ",accountType:" + account.accountType + ",balance:" + accountSequence.balance);
                    return accountSequence;
                }
            }
            lastBalance = accountSequence.balance;
        }

        return null;
    }

    /**
     * 检查账户余额、不可提现余额、活动金余额和account_sequence的最后记录的余额、不可提现余额、活动金余额是否一致.
     *
     * @param account 被检测的账户
     * @return 返回余额是否一致
     */
    public static boolean checkAndFixAccountAmount(Account account) {
        AccountSequence lastAccountSeq = AccountSequence.getLastAccountSequence(account.id, null);
        if (lastAccountSeq == null) {
            return false;
        }
        //正在申请中的不可提现总金额
        BigDecimal applyingUncashAmount = WithdrawBill.getApplyingAmountFrom(account, lastAccountSeq.createdAt);

        boolean isMatch = true;
        if (lastAccountSeq.uncashBalance != null && lastAccountSeq.uncashBalance.add(applyingUncashAmount).compareTo(account.uncashAmount) != 0) {
            account.uncashAmount = lastAccountSeq.uncashBalance.add(applyingUncashAmount);
            isMatch = false;
        }
        if (lastAccountSeq.cashBalance.subtract(applyingUncashAmount).compareTo(account.amount) != 0) {
            account.amount = lastAccountSeq.cashBalance.subtract(applyingUncashAmount);
            isMatch = false;
        }
//        if (lastAccountSeq.balance.compareTo(account.amount.add(account.uncashAmount)) != 0) {
//            account.amount = lastAccountSeq.cashBalance.subtract(applyingUncashAmount);
//            isMatch = false;
//        }
        if (lastAccountSeq.promotionBalance != null && account.promotionAmount != null && lastAccountSeq.promotionBalance.compareTo(account.promotionAmount) != 0) {
            account.promotionAmount = lastAccountSeq.promotionBalance;
            isMatch = false;
        }
        if (!isMatch) {
            //检查是否有新的sequence插入,如果有则取消修改操作
            AccountSequence currLastAccountSeq = AccountSequence.getLastAccountSequence(account.id, null);
            if (currLastAccountSeq != null && currLastAccountSeq.id.equals(lastAccountSeq.id)) {
                account.save();
                System.out.println("Fix account amount success.==> accountId:" + account.id + ",uid:" + account.uid
                        + ",accountType:" + account.accountType);
                return true;
            } else {
                System.out.println("Fix account amount failed because new sequence was inserted.==> accountId:" + account.id + ",uid:" + account.uid
                        + ",accountType:" + account.accountType);
                return false;
            }
        }
        return false;
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
     * 根据account_sequence中的changeAmount和promotionChangeAmount修改balance字段
     *
     * @param account
     */
    public static int fixAccountSequenceBalance(Account account, AccountSequence errSeq) {
        System.out.println("Begin to fix ==> accountId:" + account.id + ",uid:" + account.uid
                + ",accountType:" + account.accountType);
        //获取错误的sequence记录的前一条
        AccountSequence fromSeq = null;
        if (errSeq != null) {
            fromSeq = AccountSequence.find("account=? and id<? order by id desc", account, errSeq.id).first();
        }
        if (fromSeq == null) {
            fromSeq = errSeq;
        }
        List<AccountSequence> accountSequenceList;
        BigDecimal lastBalance = BigDecimal.ZERO;
        BigDecimal lastUncashBalance = BigDecimal.ZERO;
        BigDecimal lastPromotionBalance = BigDecimal.ZERO;

        if (fromSeq == null) {
            accountSequenceList = AccountSequence.find("account=? order by id", account).fetch();
        } else {
            accountSequenceList = AccountSequence.find("account=? and id>=? order by id", account, fromSeq.id).fetch();
        }

        boolean isChanged;
        int i = 0;
        int j = 0;
        for (AccountSequence accountSequence : accountSequenceList) {
            if (j++ == 0) {
                lastBalance = accountSequence.balance;
                lastUncashBalance = accountSequence.uncashBalance;
                lastPromotionBalance = accountSequence.promotionBalance;
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
                //System.out.println("----------- (" + i + ") fix seq:" + accountSequence.id);
                accountSequence.save();
                if ((++i) % 50 == 0) {
                    JPA.em().flush();
                }
            }

            lastUncashBalance = accountSequence.uncashBalance;
            lastPromotionBalance = accountSequence.promotionBalance;
            lastBalance = accountSequence.balance;
        }
        return i;
    }


    /**
     * 检查并修复财务流水
     *
     * @param accounts
     */
    public static void checkAndFixBalance(List<Account> accounts, Date from) {
        for (Account account : accounts) {
            AccountSequence seq = AccountSequenceUtil.checkBalance(account, from);
            if (seq == null) {
                continue;
            }

            int fixCount = fixAccountSequenceBalance(account, seq);
            System.out.println("Fixed sequence count:" + fixCount);
            //校验修复结果：通过TradeBill校验AccountSequence的修复结果
            AccountSequence lastAccountSequence = AccountSequence.getLastAccountSequence(account.id, null);
            boolean isOk = checkTradeBalance(account, lastAccountSequence);
            if (!isOk) {
                System.out.println("Fix cash balance failed because it can not match trade bill.");
            } else {
                System.out.println("Fix cash balance success.");
//                AccountSequence currentLastAccountSequence = AccountSequence.getLastAccountSequence(account.id, null);
//                if (lastAccountSequence != null && currentLastAccountSequence != null && currentLastAccountSequence.id.equals(lastAccountSequence.id)) {
//                    //如果帐号中记的现金余额和流水表中的不一致，就修改帐号表中的值
//                    if (account.amount.compareTo(currentLastAccountSequence.cashBalance) != 0) {
//                        account.amount = currentLastAccountSequence.cashBalance;
//                        account.save();
//                        System.out.println("Fix Account Amount.");
//                    }
//                    if (account.uncashAmount.compareTo(currentLastAccountSequence.uncashBalance) != 0) {
//                        account.uncashAmount = currentLastAccountSequence.uncashBalance;
//                        account.save();
//                        System.out.println("Fix Account UncashAmount.");
//                    }
//                } else {
//                    System.out.println("Account Amount didn't check and fix because new sequence was inserted.");
//                }
            }
            boolean isPromotionOk = checkTradePromotionBalance(account, lastAccountSequence);
            if (!isPromotionOk) {
                System.out.println("Fix cash balance failed because it can not match trade bill.");
            } else {
                System.out.println("Fix promotion balance success.");
//                //如果帐号中记的活动金余额和流水表中的不一致，就修改帐号表中的值
//                AccountSequence currentLastAccountSequence = AccountSequence.getLastAccountSequence(account.id, null);
//                if (lastAccountSequence != null && currentLastAccountSequence != null
//                        && currentLastAccountSequence.id.equals(lastAccountSequence.id)
//                        && account.promotionAmount.compareTo(lastAccountSequence.promotionBalance) != 0) {
//                    account.promotionAmount = lastAccountSequence.promotionBalance;
//                    account.save();
//                    System.out.println("Fix Account PromotionAmount.");
//                } else {
//                    System.out.println("Account PromotionAmount didn't check and fix because new sequence was inserted.");
//                }
            }
        }
    }


    /**
     * 检查并修改帐号的余额.
     *
     * @param accounts
     */
    public static void checkAndFixAccountAmount(List<Account> accounts) {
        for (Account account : accounts) {
            checkAndFixAccountAmount(account);
        }
    }


    /**
     * 按财务流水修改帐号余额
     *
     * @param account
     * @param mismatchBalanceList
    private static void fixAccountAmount(Account account, List<MismatchBalance> mismatchBalanceList) {
    AccountSequence lastAccountSeq = AccountSequence.getLastAccountSequence(account.id, DateUtil.getTomorrow());
    if (lastAccountSeq == null) {
    System.out.println("lastAccountSequence is null. Do not fix:account(id:" + account.id + ",uid:" + account.uid + ",type:" + account.accountType + ")");
    return;
    }
    for (MismatchBalance mismatchBalance : mismatchBalanceList) {

    switch (mismatchBalance) {
    case CASH_BALANCE:
    account.amount = lastAccountSeq.cashBalance == null ? BigDecimal.ZERO : lastAccountSeq.cashBalance;
    break;
    case UNCASH_BALANCE:
    account.uncashAmount = lastAccountSeq.uncashBalance == null ? BigDecimal.ZERO : lastAccountSeq.uncashBalance;
    break;
    case PROMOTION_BALANCE:
    account.promotionAmount = lastAccountSeq.promotionBalance == null ? BigDecimal.ZERO : lastAccountSeq.promotionBalance;
    break;
    }
    }
    account.save();
    }
     */

}
