package models.accounts.util;

import models.accounts.*;
import play.Logger;
import play.db.jpa.JPAPlugin;

import java.math.BigDecimal;

/**
 * 充值工具类
 *
 * @author likang
 * Date: 12-3-7
 */
public class ChargeUtil {

    /**
     * 创建充值流水记录.
     * 已创建的充值流水记录中包含一个自动创建的交易流水.
     *
     * @param account           被充值的账号
     * @param amount            充值金额
     * @param paymentSource     充值渠道
     * @return                  创建的充值流水记录
     */
    public static ChargeBill create(Account account, BigDecimal amount, PaymentSource paymentSource){

        TradeBill tradeBill = TradeUtil.createChargeTrade(account, amount, paymentSource);
        return createWithTrade(account, amount, tradeBill);

    }

    /**
     * 已经有交易记录的情况下创建充值流水记录，适用于临时将交易转为充值的情况.
     *
     * @param account       被充值的账号
     * @param amount        充值金额
     * @param tradeBill     交易流水
     * @return              创建的充值流水记录
     */
    public static ChargeBill createWithTrade(Account account, BigDecimal amount, TradeBill tradeBill){
        return new ChargeBill(account,
                ChargeType.EBANKING,        //默认为网银
                amount,
                tradeBill
        ).save();
    }

    /**
     * 银行返回充值成功.
     *
     * @param chargeBill    原充值交易
     * @return              修改相关状态后的充值交易
     */
    public static boolean success(ChargeBill chargeBill){
        //修改流水状态
        chargeBill.chargeStatus = ChargeStatus.SUCCESS;
        //更新账户余额
        try {
            AccountUtil.addBalance(chargeBill.account.getId(), chargeBill.amount, BigDecimal.ZERO,
                    chargeBill.getId(), AccountSequenceType.REFUND, "充值");
        } catch (BalanceNotEnoughException e) {
            Logger.error(e, e.getMessage());
            //回滚
            JPAPlugin.closeTx(true);
            return false;
        } catch (AccountNotFoundException e) {
            Logger.error(e, e.getMessage());
            //回滚
            JPAPlugin.closeTx(true);
            return false;
        }

        chargeBill.save();
        return true;
    }


}
