package models.accounts.util;

import models.accounts.*;

import java.math.BigDecimal;
import java.util.MissingFormatArgumentException;

/**
 * 账户资金变动流水工具.
 *
 * @author likang
 * Date: 12-3-7
 */

public class AccountUtil {

    public static Account getUhuilaAccount(){
        return getAccount(Account.UHUILA_COMMISSION, AccountType.PLATFORM);
    }

    public static Account getPlatformIncomingAccount(){
        return getAccount(Account.PLATFORM_INCOMING, AccountType.PLATFORM);
    }

    public static Account getPlatformCommissionAccount(){
        return getAccount(Account.PLATFORM_COMMISSION, AccountType.PLATFORM);
    }

    public static Account getPlatformWithdrawAccount(){
        return getAccount(Account.PLATFORM_WITHDRAW, AccountType.PLATFORM);
    }

    public static boolean accountExist(long uid, AccountType type){
        return Account.find("byUidAndAccountType", uid, type).first() != null;
    }

    public static Account getAccount(long uid, AccountType type){
        Account account = Account.find("byUidAndAccountType", uid, type).first();
        if(account == null){
            synchronized(Account.class){
                account = Account.find("byUidAndAccountType", uid, type).first();
                if(account == null){
                    return new Account(uid, type).save();
                }
            }
        }
        return account;
    }


    /*
     * 变更账户余额，同时不保存凭证相关信息.
     */
    public static Account addBalanceWithoutSavingSequence(Long accountId, BigDecimal cashAugend,
                                                  BigDecimal uncashAugend, Long billId, String note, Long orderId)
            throws BalanceNotEnoughException,AccountNotFoundException{
        return addBalance(accountId, cashAugend, uncashAugend, billId, null, null, note, orderId, false);
    }


    /*
     * 变更账户余额，同时保存凭证相关信息.
     */
    public static Account addBalanceAndSaveSequence(Long accountId, BigDecimal cashAugend, BigDecimal uncashAugend, Long billId,
                         TradeType tradeType, AccountSequenceFlag sequenceFlag, String note, Long orderId)
            throws BalanceNotEnoughException,AccountNotFoundException{
        return addBalance(accountId, cashAugend, uncashAugend, billId, tradeType, sequenceFlag, note, orderId, true);
    }

    /**
     * 变更账户余额，同时根据需要保存凭证相关信息.
     *
     * @param accountId     需要变更的账户ID
     * @param cashAugend    可提现金额变更额度
     * @param uncashAugend  不可提现金额变更额度
     * @param billId        关联的交易ID
     * @param tradeType     交易类型
     * @param sequenceFlag  账户资金变动类型,进账or出账
     * @param note          变动备注
     * @return              变更后的账户
     */
    private static Account addBalance(Long accountId, BigDecimal cashAugend, BigDecimal uncashAugend, Long billId,
                TradeType tradeType, AccountSequenceFlag sequenceFlag, String note, Long orderId, boolean saveSequence)
            throws BalanceNotEnoughException,AccountNotFoundException{

        Account account = Account.findById(accountId);
        if(account == null){
            throw new AccountNotFoundException("can not find the specified account:" + accountId);
        }

        if(billId == null || ( saveSequence && (tradeType== null || sequenceFlag == null))){
            throw new IllegalArgumentException("error while add balance to account: miss parameter");
        }

        if (account.amount.add(cashAugend).compareTo(BigDecimal.ZERO) >= 0 ){
            account.amount = account.amount.add(cashAugend);
        }else {
            throw new BalanceNotEnoughException("error while add cash to account: balance not enough");
        }
        if (account.uncashAmount.add(uncashAugend).compareTo(BigDecimal.ZERO) >= 0){
            account.uncashAmount = account.uncashAmount.add(uncashAugend);
        }else {
            throw new BalanceNotEnoughException("error while add uncashAmount to account: balance not enough");
        }

        account.save();
        if(saveSequence){
            accountChanged(account, cashAugend.add(uncashAugend), billId, tradeType, sequenceFlag, note, orderId);
        }
        return account;
    }

    private static void accountChanged(Account account, BigDecimal amount, Long billId,
            TradeType tradeType, AccountSequenceFlag sequenceFlag, String note, Long orderId){
        if(account == null){
            throw new IllegalArgumentException("accountChanged: account can not be null");
        }
        //保存账户变动信息
        AccountSequence accountSequence = new AccountSequence(
                account,
                sequenceFlag,                                       //账务变动方向
                tradeType,                                          //变动类型
                amount,                                             //变动金额
                account.amount.add(account.uncashAmount),           //变动后总余额
                account.amount,                                     //变动后可提现余额
                account.uncashAmount,                               //变动后不可提现余额
                billId);                                            //相关流水号
        accountSequence.remark = note;
        accountSequence.orderId = orderId;
        accountSequence.save();                                     //保存账户变动信息
    }
    
}
