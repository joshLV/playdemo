package models.accounts.util;

import models.accounts.*;
import play.Logger;

import java.math.BigDecimal;

/**
 * 账户资金变动流水工具.
 *
 * @author likang
 * Date: 12-3-7
 */

public class AccountUtil {
    private static final long uhuilaUid = 1;

    public static Account getUhuilaAccount(){
        Account account =  Account.find("byUid", uhuilaUid).first();
        if(account == null){
            throw new RuntimeException("can not get uhuila account"); //未建立uhuila账户
        }
        return account;
    }


    /**
     * 变更账户可余额，同时保存凭证相关信息.
     *
     * @param account       需要变更的账户
     * @param augend        变更额度
     * @param billId        关联的交易ID
     * @param sequenceType  账户资金变动类型
     * @param note          变动备注
     * @return              变更后的账户
     */
    public static Account addCash(Account account, BigDecimal augend, Long billId, AccountSequenceType sequenceType,
                                  String note){
        if(billId == null || sequenceType == null){
            throw new RuntimeException("error while add cash to account: miss parameter");
        }

        if ( account.amount.add(augend).compareTo(BigDecimal.ZERO) >= 0 ){
            account.amount = account.amount.add(augend);
        }
        account.save();
        accountChanged(account, augend, billId, sequenceType, note);
        return account;
    }

    private static void accountChanged(Account account, BigDecimal amount, Long billId,
                                       AccountSequenceType sequenceType, String note){
        //保存账户变动信息
        AccountSequence accountSequence = new AccountSequence(
                account,
                AccountSequenceFlag.VOSTRO,                           //账务变动方向：来帐
                sequenceType,                                         //变动类型
                account.amount.subtract(amount),                      //变动前资金
                account.amount,                                       //变动后资金
                amount,                                               //可提现金额
                BigDecimal.ZERO,                                      //不可提现金额
                billId);                                              //相关流水号
        accountSequence.save();                                       //保存账户变动信息

        //保存凭证明细
        CertificateDetail certificateDetail = new CertificateDetail(
                amount.compareTo(BigDecimal.ZERO) > 0 ? CertificateType.DEBIT : CertificateType.CREDIT,
                amount,
                billId,
                note
        );
        certificateDetail.save();

        //保存两个科目明细
        SubjectDetail subjectDetailA = new SubjectDetail(
                amount.compareTo(BigDecimal.ZERO) > 0 ? SubjectType.DEBIT : SubjectType.CREDIT,
                certificateDetail,
                amount,
                BigDecimal.ZERO,
                note
        );
        subjectDetailA.save();

        SubjectDetail subjectDetailB = new SubjectDetail(
                amount.compareTo(BigDecimal.ZERO) < 0 ? SubjectType.DEBIT : SubjectType.CREDIT,
                certificateDetail,
                BigDecimal.ZERO,
                amount,
                note
        );
        subjectDetailB.save();
    }
    
}
