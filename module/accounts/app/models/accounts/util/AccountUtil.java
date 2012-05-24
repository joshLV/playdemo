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


    /**
     * 变更账户余额，同时保存凭证相关信息.
     *
     * @param accountId     需要变更的账户ID
     * @param cashAugend    可提现金额变更额度
     * @param uncashAugend  不可提现金额变更额度
     * @param billId        关联的交易ID
     * @param sequenceType  账户资金变动类型
     * @param note          变动备注
     * @return              变更后的账户
     */
    public static Account addBalance(Long accountId, BigDecimal cashAugend, BigDecimal uncashAugend, Long billId,
                                     AccountSequenceType sequenceType, String note, Long orderId)
            throws BalanceNotEnoughException,AccountNotFoundException{

        Account account = Account.findById(accountId);
        if(account == null){
            throw new AccountNotFoundException("can not find the specified account:" + accountId);
        }

        if(billId == null || sequenceType == null){
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
        //申请提现或者是提现被拒绝
        if (sequenceType == AccountSequenceType.FREEZE || sequenceType == AccountSequenceType.UNFREEZE){
            return account;
        }
        //提现成功
        if(sequenceType == AccountSequenceType.WITHDRAW){
            accountChanged(account.getId(), uncashAugend, BigDecimal.ZERO, billId, sequenceType, note, orderId);
            return account;
        }
        accountChanged(account.getId(), cashAugend, uncashAugend, billId, sequenceType, note, orderId);
        return account;
    }

    private static void accountChanged(Long accountId, BigDecimal cashAmount, BigDecimal uncashAmount,
                                       Long billId, AccountSequenceType sequenceType, String note, Long orderId){
        Account account = Account.findById(accountId);
        if(account == null){
            throw new RuntimeException("can not find the specified account:" + accountId);
        }
        AccountSequenceFlag flag = AccountSequenceFlag.VOSTRO;
        if(sequenceType == AccountSequenceType.PAY || sequenceType == AccountSequenceType.PAY_REFUND){
            flag = AccountSequenceFlag.NOSTRO;
        }

        //保存账户变动信息
        AccountSequence accountSequence = new AccountSequence(
                account,
                flag,                                               //账务变动方向
                sequenceType,                                       //变动类型
                account.amount.subtract(cashAmount),                //变动前资金
                account.amount,                                     //变动后资金
                cashAmount,                                         //可提现金额
                uncashAmount,                                       //不可提现金额
                billId);                                            //相关流水号
        accountSequence.remark = note;
        accountSequence.orderId = orderId;
        accountSequence.save();                                     //保存账户变动信息

        //保存凭证明细
        CertificateDetail certificateDetail = new CertificateDetail(
                cashAmount.compareTo(BigDecimal.ZERO) > 0 ? CertificateType.DEBIT : CertificateType.CREDIT,
                cashAmount,
                billId,
                note
        );
        certificateDetail.save();

        //保存两个科目明细
        SubjectDetail subjectDetailA = new SubjectDetail(
                cashAmount.compareTo(BigDecimal.ZERO) > 0 ? SubjectType.DEBIT : SubjectType.CREDIT,
                certificateDetail,
                cashAmount,
                uncashAmount,
                note
        );
        subjectDetailA.save();

        SubjectDetail subjectDetailB = new SubjectDetail(
                cashAmount.compareTo(BigDecimal.ZERO) < 0 ? SubjectType.DEBIT : SubjectType.CREDIT,
                certificateDetail,
                uncashAmount,
                cashAmount,
                note
        );
        subjectDetailB.save();
    }
    
}
