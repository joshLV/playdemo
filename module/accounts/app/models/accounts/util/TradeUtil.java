package models.accounts.util;

import models.accounts.*;
import play.Logger;

import java.math.BigDecimal;

/**
 * 交易记录工具类
 *
 * @author likang
 *         Date: 12-3-7
 */
public class TradeUtil {

    /**
     * 创建订单交易.
     *
     * @param account              支付订单的账户
     * @param balancePaymentAmount 账户余额中应扣款
     * @param ebankPaymentAmount   网银应支付款
     * @param paymentSource        网银信息
     * @param orderId              关联的订单
     * @return 创建的订单交易记录
     */
    public static TradeBill createOrderTrade(
            Account account, BigDecimal balancePaymentAmount,
            BigDecimal ebankPaymentAmount, PaymentSource paymentSource, Long orderId) {
        if (account == null) {
            Logger.error("error while create order trade: no account specified");
            return null;
        }
        if (balancePaymentAmount == null || balancePaymentAmount.compareTo(BigDecimal.ZERO) < 0) {
            Logger.error("error while create order trade: invalid balancePaymentAmount");
            return null;
        }
        if (ebankPaymentAmount == null || ebankPaymentAmount.compareTo(BigDecimal.ZERO) < 0) {
            Logger.error("error while create order trade: invalid ebankPaymentAmount");
            return null;
        }
        if (paymentSource == null) {
            Logger.error("error while create order trade: invalid paymentSource");
            return null;
        }
        if (orderId == null) {
            Logger.error("error while create order trade: invalid orderId");
            return null;
        }

        TradeBill tradeBill = new TradeBill();
        tradeBill.fromAccount          = account;                                  //付款方账户
        tradeBill.toAccount            = AccountUtil.getPlatformIncomingAccount(); //默认收款账户为平台收款账户
        tradeBill.balancePaymentAmount = balancePaymentAmount;                     //使用余额支付金额
        tradeBill.ebankPaymentAmount   = ebankPaymentAmount;                       //使用网银支付金额
        tradeBill.tradeType            = TradeType.PAY;                            //交易类型为支付
        tradeBill.paymentSource        = paymentSource;                            //银行信息
        tradeBill.orderId              = orderId;                                  //订单信息
        tradeBill.amount = tradeBill.balancePaymentAmount.add(tradeBill.ebankPaymentAmount);
        
        return tradeBill.save();
    }

    /**
     * 创建充值交易.
     *
     * @param account       充值账户
     * @param amount        充值金额
     * @param paymentSource 网银信息
     * @return 创建的订单交易记录
     */
    public static TradeBill createChargeTrade(Account account, BigDecimal amount, PaymentSource paymentSource) {
        if (account == null) {
            Logger.error("error while create order trade: no account specified");
            return null;
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            Logger.error("error while create order trade: invalid balancePaymentAmount");
            return null;
        }
        if (paymentSource == null) {
            Logger.error("error while create order trade: invalid paymentSource");
            return null;
        }
        
        TradeBill tradeBill = new TradeBill();
        tradeBill.fromAccount          = account;           //付款方账户为本人
        tradeBill.toAccount            = account;           //收款方账户为本人
        tradeBill.balancePaymentAmount = BigDecimal.ZERO;   //付款方不使用余额支付
        tradeBill.ebankPaymentAmount   = amount;            //充值金额全部使用网银支付
        tradeBill.tradeType            = TradeType.CHARGE;  //交易类型为充值
        tradeBill.paymentSource        = paymentSource;     //银行信息
        tradeBill.amount = tradeBill.balancePaymentAmount.add(tradeBill.ebankPaymentAmount);
        
        return tradeBill.save();
    }
    
    /**
     * 创建消费交易，
     * 消费者消费成功,资金从平台收款账户转到商户
     *
     * @param eCouponSn 券号
     * @param account  商户账户
     * @param amount   券的进货价
     * @return 消费交易记录
     */
    public static TradeBill createConsumeTrade(String eCouponSn, Account account, BigDecimal amount) {
        if (account == null) {
            Logger.error("error while create consume trade: invalid account");
            return null;
        }
        if (eCouponSn == null) {
            Logger.error("error while create consume trade: invalid eCouponSn");
            return null;
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            Logger.error("error while create consume trade: invalid consumePrice");
            return null;
        }

        TradeBill tradeBill = new TradeBill();
        tradeBill.fromAccount          = AccountUtil.getPlatformIncomingAccount();  //付款方账户为平台收款账户
        tradeBill.toAccount            = account;                                   //商户账户
        tradeBill.balancePaymentAmount = amount;                                    //全部使用平台收款账户的余额支付
        tradeBill.ebankPaymentAmount   = BigDecimal.ZERO;                           //不使用网银支付
        tradeBill.tradeType            = TradeType.CONSUME;                         //交易类型为佣金
        tradeBill.eCouponSn            = eCouponSn;                                 //保存对应的券号，以便核查
        tradeBill.amount = tradeBill.balancePaymentAmount.add(tradeBill.ebankPaymentAmount);

        return tradeBill.save();
    }
    
    /**
     * 创建佣金交易，消费者消费成功后，将佣金付给平台和优惠啦
     * 
     * @param account   收取佣金的账户
     * @param amount    佣金金额
     * @param eCouponSn 对应的电子券号
     * @return
     */
    public static TradeBill createCommissionTrade(Account account, BigDecimal amount, String eCouponSn){
        if (account == null) {
            Logger.error("error while create commission trade: invalid account");
            return null;
        }
        if (eCouponSn == null) {
            Logger.error("error while create commission trade: invalid eCouponSn");
            return null;
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            Logger.error("error while create commission trade: invalid amount");
            return null;
        }

        TradeBill tradeBill = new TradeBill();
        tradeBill.fromAccount          = AccountUtil.getPlatformIncomingAccount();  //付款方账户为平台收款账户
        tradeBill.toAccount            = account;                                   //收款方账户
        tradeBill.balancePaymentAmount = amount;                                    //全部使用平台收款账户的余额支付
        tradeBill.ebankPaymentAmount   = BigDecimal.ZERO;                           //不使用网银支付
        tradeBill.tradeType            = TradeType.COMMISSION;                      //交易类型为佣金
        tradeBill.eCouponSn            = eCouponSn;                                 //保存对应的券号，以便核查
        tradeBill.amount = tradeBill.balancePaymentAmount.add(tradeBill.ebankPaymentAmount);
        
        return tradeBill.save();
    }

    /**
     * 创建转账交易.
     *
     * @param fromAccount          //发起人账户
     * @param toAccount            //收款人账户
     * @param balancePaymentAmount //使用余额支付额
     * @param ebankPaymentAmount   //使用网银支付额
     * @param paymentSource        //网银信息
     * @return //创建的转账交易记录
     */
    public static TradeBill createTransferTrade(Account fromAccount, Account toAccount,
                                                BigDecimal balancePaymentAmount, BigDecimal ebankPaymentAmount, PaymentSource paymentSource) {

        if (fromAccount == null || toAccount == null) {
            Logger.error("error while create transfer trade: invalid fromAccount or toAccount");
            return null;
        }
        if (balancePaymentAmount == null || balancePaymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            Logger.error("error while create transfer trade: invalid balancePaymentAmount");
            return null;
        }
        if (ebankPaymentAmount == null || ebankPaymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            Logger.error("error while create transfer trade: invalid ebankPaymentAmount");
            return null;
        }

        if (paymentSource == null) {
            Logger.error("error while create order trade: invalid paymentSource");
            return null;
        }

        TradeBill tradeBill = new TradeBill();
        tradeBill.fromAccount          = fromAccount;           //付款方账户
        tradeBill.toAccount            = toAccount;             //收款方账户
        tradeBill.balancePaymentAmount = balancePaymentAmount;  //不使用余额支付金额
        tradeBill.ebankPaymentAmount   = ebankPaymentAmount;    //使用网银支付金额
        tradeBill.tradeType            = TradeType.TRANSFER;    //交易类型
        tradeBill.paymentSource        = paymentSource;         //银行信息
        tradeBill.amount = tradeBill.balancePaymentAmount.add(tradeBill.ebankPaymentAmount);
        
        return tradeBill.save();
    }


    /**
     * 交易成功
     *
     * @param tradeBill 需变更的交易记录
     * @return 更新后的交易记录
     */
    public static TradeBill success(TradeBill tradeBill) {
        tradeBill.tradeStatus = TradeStatus.SUCCESS;

        //余额不足以支付订单中指定的使用余额付款的金额
        //则将充值的钱打入发起人账户里
        if (tradeBill.fromAccount.amount.compareTo(tradeBill.balancePaymentAmount) < 0) {
            if (tradeBill.ebankPaymentAmount.compareTo(BigDecimal.ZERO) >= 0) {
                ChargeBill chargeBill
	                = ChargeUtil.createWithTrade(tradeBill.fromAccount, tradeBill.ebankPaymentAmount, tradeBill);
                ChargeUtil.success(chargeBill);
	
                return tradeBill;
            }else {
                return null;
            }
        }
        if (tradeBill.balancePaymentAmount.compareTo(BigDecimal.ZERO) >= 0) {
            AccountUtil.addCash(
                    tradeBill.fromAccount,
                    tradeBill.balancePaymentAmount.negate(),
                    tradeBill.getId(),
                    AccountSequenceType.PAY,
                    "支付");
        }
        if (tradeBill.ebankPaymentAmount.add(tradeBill.balancePaymentAmount).compareTo(BigDecimal.ZERO) >= 0) {
            AccountUtil.addCash(
                    tradeBill.toAccount,
                    tradeBill.ebankPaymentAmount.add(tradeBill.balancePaymentAmount),
                    tradeBill.getId(),
                    AccountSequenceType.RECEIVE,
                    "收款");
        }

        return tradeBill.save();
    }
}
