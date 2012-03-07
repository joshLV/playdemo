package models.accounts.util;

import models.accounts.*;
import play.Logger;

import java.math.BigDecimal;

/**
 * 交易记录工具类
 *
 * @author likang
 * Date: 12-3-7
 */
public class TradeUtil {

    /**
     * 创建订单交易.
     *
     * @param account               支付订单的账户
     * @param balancePaymentAmount  账户余额中应扣款
     * @param ebankPaymentAmount    网银应支付款
     * @param paymentSource         网银信息
     * @param orderId               关联的订单
     * @return                      创建的订单交易记录
     */
    public static TradeBill createOrderTrade(
            Account account, BigDecimal balancePaymentAmount,
            BigDecimal ebankPaymentAmount, PaymentSource paymentSource, Long orderId){
        if(account == null) {
            Logger.error("error while create order trade: no account specified");
            return null;
        }
        if(balancePaymentAmount != null && balancePaymentAmount.compareTo(BigDecimal.ZERO) <= 0){
            Logger.error("error while create order trade: invalid balancePaymentAmount");
            return null;
        }
        if(ebankPaymentAmount != null && ebankPaymentAmount.compareTo(BigDecimal.ZERO) <= 0){
            Logger.error("error while create order trade: invalid ebankPaymentAmount");
            return null;
        }
        if(paymentSource == null){
            Logger.error("error while create order trade: invalid paymentSource");
            return null;
        }
        if(orderId == null){
            Logger.error("error while create order trade: invalid orderId");
            return null;
        }

        return new TradeBill(account,
                AccountUtil.getUhuilaAccount(),  //默认使用uhuila账户作为收款账户
                balancePaymentAmount,
                ebankPaymentAmount,
                TradeType.PAY,
                paymentSource,
                orderId
        ).save();
    }

    /**
     * 创建充值交易.
     *
     * @param account           充值账户
     * @param amount            充值金额
     * @param paymentSource     网银信息
     * @return                  创建的订单交易记录
     */
    public static TradeBill createChargeTrade(Account account, BigDecimal amount, PaymentSource paymentSource){
        if(account == null) {
            Logger.error("error while create order trade: no account specified");
            return null;
        }
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            Logger.error("error while create order trade: invalid balancePaymentAmount");
            return null;
        }
        if(paymentSource == null){
            Logger.error("error while create order trade: invalid paymentSource");
            return null;
        }

        return new TradeBill(account,   //付款方账户为本人
                account,                //收款方账户为本人
                BigDecimal.ZERO,        //付款方不使用余额支付
                amount,                 //充值金额全部使用网银支付
                TradeType.CHARGE,       //交易类型为充值
                paymentSource,          //银行信息
                null
        ).save();
    }

    /**
     * 创建转账交易.
     *
     * @param fromAccount               //发起人账户
     * @param toAccount                 //收款人账户
     * @param balancePaymentAmount      //使用余额支付额
     * @param ebankPaymentAmount        //使用网银支付额
     * @param paymentSource             //网银信息
     * @return                          //创建的转账交易记录
     */
    public static TradeBill createTransferTrade(Account fromAccount, Account toAccount,
            BigDecimal balancePaymentAmount, BigDecimal ebankPaymentAmount, PaymentSource paymentSource){

        if(fromAccount == null || toAccount == null){
            Logger.error("error while create transfer trade: invalid fromAccount or toAccount");
            return null;
        }
        if(balancePaymentAmount != null && balancePaymentAmount.compareTo(BigDecimal.ZERO) <= 0){
            Logger.error("error while create transfer trade: invalid balancePaymentAmount");
            return null;
        }
        if(ebankPaymentAmount != null && ebankPaymentAmount.compareTo(BigDecimal.ZERO) <= 0){
            Logger.error("error while create transfer trade: invalid ebankPaymentAmount");
            return null;
        }

        if(paymentSource == null){
            Logger.error("error while create order trade: invalid paymentSource");
            return null;
        }

        return new TradeBill(fromAccount,
                toAccount,
                balancePaymentAmount,
                ebankPaymentAmount,
                TradeType.TRANSFER,
                paymentSource,
                null
        ).save();
    }


    /**
     * 网银支付成功
     *
     * @param tradeBill     需变更的交易记录
     * @return              更新后的交易记录
     */
    public static TradeBill success(TradeBill tradeBill){
        tradeBill.tradeStatus = TradeStatus.SUCCESS;

        //余额不足以支付订单中指定的使用余额付款的金额
        //则将充值的钱打入发起人账户里
        if(tradeBill.fromAccount.amount.compareTo(tradeBill.balancePaymentAmount) < 0){

            AccountUtil.addCash(
                    tradeBill.fromAccount,
                    tradeBill.ebankPaymentAmount,
                    tradeBill.getId(),
                    AccountSequenceType.CHARGE,
                    "账户充值");

            ChargeBill chargeBill
                    = ChargeUtil.createWithTrade(tradeBill.fromAccount,tradeBill.ebankPaymentAmount,tradeBill);
            ChargeUtil.success(chargeBill);

            return tradeBill;
        }

        tradeBill.save();

        AccountUtil.addCash(
                tradeBill.fromAccount,
                BigDecimal.ZERO.subtract(tradeBill.balancePaymentAmount),
                tradeBill.getId(),
                AccountSequenceType.PAY,
                "支付");
        AccountUtil.addCash(
                tradeBill.toAccount,
                tradeBill.ebankPaymentAmount.add(tradeBill.balancePaymentAmount),
                tradeBill.getId(),
                AccountSequenceType.RECEIVE,
                "收款");

        return tradeBill;
    }
}
