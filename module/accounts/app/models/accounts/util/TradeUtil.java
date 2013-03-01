package models.accounts.util;

import models.accounts.Account;
import models.accounts.AccountNotFoundException;
import models.accounts.AccountSequenceFlag;
import models.accounts.BalanceNotEnoughException;
import models.accounts.PaymentSource;
import models.accounts.SettlementStatus;
import models.accounts.TradeBill;
import models.accounts.TradeStatus;
import models.accounts.TradeType;
import models.accounts.WithdrawBill;
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
     * @param account                支付订单的账户
     * @param balancePaymentAmount   账户余额中应扣款
     * @param ebankPaymentAmount     网银应支付款
     * @param uncashPaymentAmount    不可提现余额应扣款
     * @param promotionPaymentAmount 活动金使用
     * @param paymentSource          网银信息
     * @param orderId                关联的订单
     * @return 创建的订单交易记录
     */
    public static TradeBill createOrderTrade(Account account, BigDecimal balancePaymentAmount, BigDecimal ebankPaymentAmount,
                                             BigDecimal uncashPaymentAmount, BigDecimal promotionPaymentAmount, PaymentSource paymentSource, Long orderId) {
        if (account == null) {
            throw new IllegalArgumentException("error while create order trade: no account specified");
        }
        if (balancePaymentAmount == null || balancePaymentAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("error while create order trade. invalid balancePaymentAmount: " + balancePaymentAmount);
        }
        if (ebankPaymentAmount == null || ebankPaymentAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("error while create order trade. invalid ebankPaymentAmount: " + ebankPaymentAmount);
        }
        if (uncashPaymentAmount == null || uncashPaymentAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("error while create order trade. invalid uncashPaymentAmount: " + uncashPaymentAmount);
        }
        if (paymentSource == null) {
            throw new IllegalArgumentException("error while create order trade: invalid paymentSource");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("error while create order trade: invalid orderId");
        }

        TradeBill tradeBill = new TradeBill();
        tradeBill.fromAccount = account;                                  //付款方账户
        tradeBill.toAccount = AccountUtil.getPlatformIncomingAccount(); //默认收款账户为平台收款账户
        tradeBill.balancePaymentAmount = balancePaymentAmount;                     //使用余额支付金额
        tradeBill.ebankPaymentAmount = ebankPaymentAmount;                       //使用网银支付金额
        tradeBill.uncashPaymentAmount = uncashPaymentAmount;                      //使用不可提现余额支付金额
        tradeBill.promotionPaymentAmount = promotionPaymentAmount;                   //使用活动金余额支付金额
        tradeBill.tradeType = TradeType.PAY;                            //交易类型为支付
        tradeBill.paymentSource = paymentSource;                            //银行信息
        tradeBill.orderId = orderId;                                  //订单信息
        tradeBill.amount = tradeBill.balancePaymentAmount
                .add(tradeBill.ebankPaymentAmount)
                .add(tradeBill.uncashPaymentAmount)
                .add(promotionPaymentAmount);

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
    public static TradeBill createChargeTrade(Account account, BigDecimal amount,
                                              PaymentSource paymentSource, Long orderId) {
        if (account == null) {
            throw new IllegalArgumentException("error while create charge trade: no account specified");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("error while create charge trade. invalid amount: " + amount);
        }
        if (paymentSource == null) {
            throw new IllegalArgumentException("error while create charge trade: invalid paymentSource");
        }

        TradeBill tradeBill = new TradeBill();
        tradeBill.fromAccount = AccountUtil.getPaymentPartnerAccount(paymentSource.paymentCode);//付款方为第三方支付的虚拟账户
        tradeBill.toAccount = account;           //收款方账户为本人
        tradeBill.balancePaymentAmount = BigDecimal.ZERO;   //付款方不使用余额支付
        tradeBill.ebankPaymentAmount = amount;            //充值金额全部使用网银支付
        tradeBill.uncashPaymentAmount = BigDecimal.ZERO;   //付款方不使用不可提现余额支付
        tradeBill.tradeType = TradeType.CHARGE;  //交易类型为充值
        tradeBill.paymentSource = paymentSource;     //银行信息
        tradeBill.amount = tradeBill.balancePaymentAmount
                .add(tradeBill.ebankPaymentAmount)
                .add(tradeBill.uncashPaymentAmount);
        tradeBill.orderId = orderId;

        return tradeBill.save();
    }

    public static TradeBill createPromotionChargeTrade(Account account, BigDecimal amount, Long orderId) {
        if (account == null) {
            throw new IllegalArgumentException("error while create charge trade: no account specified");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("error while create charge trade. invalid amount: " + amount);
        }

        TradeBill tradeBill = new TradeBill();
        tradeBill.fromAccount = AccountUtil.getPromotionAccount();
        tradeBill.toAccount = account;          //收款方账户为本人
        tradeBill.balancePaymentAmount = BigDecimal.ZERO;  //付款方不使用余额支付
        tradeBill.ebankPaymentAmount = BigDecimal.ZERO;  //充值金额不使用网银支付
        tradeBill.uncashPaymentAmount = BigDecimal.ZERO;  //付款方不使用不可提现余额支付
        tradeBill.promotionPaymentAmount = amount;           //付款方全部使用活动金支付
        tradeBill.tradeType = TradeType.CHARGE; //交易类型为充值
        tradeBill.amount = tradeBill.balancePaymentAmount
                .add(tradeBill.ebankPaymentAmount)
                .add(tradeBill.uncashPaymentAmount)
                .add(tradeBill.promotionPaymentAmount);
        tradeBill.orderId = orderId;

        return tradeBill.save();
    }

    public static TradeBill createConsumeTrade(String eCouponSn, Account account, BigDecimal amount, Long orderId) {
        return createConsumeTrade(eCouponSn, account, amount, orderId, false);
    }

    /**
     * 创建消费交易，
     * 消费者消费成功,资金从平台收款账户转到商户
     *
     * @param eCouponSn 券号
     * @param account   商户账户
     * @param amount    券的进货价
     * @return 消费交易记录
     */
    public static TradeBill createConsumeTrade(String eCouponSn, Account account, BigDecimal amount, Long orderId, boolean reverse) {
        if (account == null) {
            throw new IllegalArgumentException("error while create consume trade: invalid account");
        }
        if (eCouponSn == null) {
            throw new IllegalArgumentException("error while create consume trade: invalid eCouponSn");
        }
        if (amount == null) {
            throw new IllegalArgumentException("error while create consume trade. invalid amount: " + amount);
        }

        TradeBill tradeBill = new TradeBill();
        if (!reverse) {
            tradeBill.fromAccount = AccountUtil.getPlatformIncomingAccount();  //付款方账户为平台收款账户
            tradeBill.toAccount = account;                                   //商户账户
        } else {
            //目前只有已消费退款会发生这样的情况
            tradeBill.fromAccount = account;                                   //商户账户
            tradeBill.toAccount = AccountUtil.getPlatformIncomingAccount();  //收款方账户为平台收款账户
        }
        tradeBill.balancePaymentAmount = amount;                                    //全部使用平台收款账户的余额支付
        tradeBill.ebankPaymentAmount = BigDecimal.ZERO;                           //不使用网银支付
        tradeBill.uncashPaymentAmount = BigDecimal.ZERO;                           //不使用不可提现余额支付
        tradeBill.tradeType = TradeType.PURCHASE_COSTING;                //交易类型为支付费用
        tradeBill.eCouponSn = eCouponSn;                                 //保存对应的券号，以便核查
        tradeBill.amount = tradeBill.balancePaymentAmount
                .add(tradeBill.ebankPaymentAmount)
                .add(tradeBill.uncashPaymentAmount);
        tradeBill.orderId = orderId;

        Logger.info("try to save tradeBill:" + tradeBill);
        return tradeBill.save();
    }

    public static TradeBill createCommissionTrade(Account account, BigDecimal amount, String eCouponSn, Long orderId) {
        return createCommissionTrade(account, amount, eCouponSn, orderId, false);
    }

    /**
     * 创建佣金交易，消费者消费成功后，将佣金付给平台和一百券
     *
     * @param account   收取佣金的账户
     * @param amount    佣金金额
     * @param eCouponSn 对应的电子券号
     * @return 新建立的佣金交易信息
     */
    public static TradeBill createCommissionTrade(Account account, BigDecimal amount, String eCouponSn, Long orderId, boolean reverse) {
        if (account == null) {
            throw new IllegalArgumentException("error while create commission trade: invalid account");
        }
        if (eCouponSn == null) {
            throw new IllegalArgumentException("error while create commission trade: invalid eCouponSn");
        }
        if (amount == null) {
            throw new IllegalArgumentException("error while create commission trade. invalid amount: " + amount);
        }

        TradeBill tradeBill = new TradeBill();
        if (!reverse) {
            tradeBill.fromAccount = AccountUtil.getPlatformIncomingAccount();  //付款方账户为平台收款账户
            tradeBill.toAccount = account;                                   //收款方账户
        } else {
            //目前只有已消费退款会发生这样的情况
            tradeBill.fromAccount = account;                                   //付款方账户
            tradeBill.toAccount = AccountUtil.getPlatformIncomingAccount();  //收款方账户为平台收款账户
        }
        tradeBill.balancePaymentAmount = amount;                                    //全部使用平台收款账户的余额支付
        tradeBill.ebankPaymentAmount = BigDecimal.ZERO;                           //不使用网银支付
        tradeBill.uncashPaymentAmount = BigDecimal.ZERO;                           //不使用不可提现余额支付
        tradeBill.tradeType = TradeType.COMMISSION;                      //交易类型为佣金
        tradeBill.eCouponSn = eCouponSn;                                 //保存对应的券号，以便核查
        tradeBill.amount = tradeBill.balancePaymentAmount
                .add(tradeBill.ebankPaymentAmount)
                .add(tradeBill.uncashPaymentAmount);
        tradeBill.orderId = orderId;

        return tradeBill.save();
    }

    /**
     * 创建运费交易，发货后,平台佣金账户将收取运费
     *
     * @param account 收取佣金的账户,目前请设置为平台佣金账户
     * @param amount  运费金额
     * @return 新建立的运费交易信息
     */
    public static TradeBill createFreightTrade(Account account, BigDecimal amount, Long orderId) {
        if (account == null) {
            throw new IllegalArgumentException("error while create commission trade: invalid account");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("error while create commission trade. invalid amount: " + amount);
        }

        TradeBill tradeBill = new TradeBill();
        tradeBill.fromAccount = AccountUtil.getPlatformIncomingAccount();  //付款方账户为平台收款账户
        tradeBill.toAccount = account;                                   //收款方账户
        tradeBill.balancePaymentAmount = amount;                                    //全部使用平台收款账户的余额支付
        tradeBill.ebankPaymentAmount = BigDecimal.ZERO;                           //不使用网银支付
        tradeBill.uncashPaymentAmount = BigDecimal.ZERO;                           //不使用不可提现余额支付
        tradeBill.tradeType = TradeType.FREIGHT;                         //交易类型为运费
        tradeBill.eCouponSn = "";                                        //保存对应的券号，以便核查
        tradeBill.amount = tradeBill.balancePaymentAmount
                .add(tradeBill.ebankPaymentAmount)
                .add(tradeBill.uncashPaymentAmount);
        tradeBill.orderId = orderId;

        return tradeBill.save();
    }

    /**
     * 创建提现交易,提现成功后,账户的不可用余额将减少
     * <p/>
     * 注意,只有在提现审批通过时才有必要创建此trade,申请和拒绝时不必创建
     *
     * @param account 提现账户
     * @param amount  提现金额
     * @return 新建的提现交易
     */
    public static TradeBill createWithdrawTrade(Account account, BigDecimal amount, Long withdrawBillId) {
        if (account == null) {
            throw new IllegalArgumentException("error while create withdraw trade: invalid account");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("error while create withdraw trade. invalid amount: " + amount);
        }
        TradeBill tradeBill = new TradeBill();
        tradeBill.fromAccount = account;                                  //付款方为提款账户
        tradeBill.toAccount = AccountUtil.getPlatformWithdrawAccount(); //收款方账户为平台提现收款账户
        tradeBill.balancePaymentAmount = BigDecimal.ZERO;                          //不使用可提现余额支付
        tradeBill.ebankPaymentAmount = BigDecimal.ZERO;                          //不使用网银支付
        tradeBill.uncashPaymentAmount = amount;                                   //全部使用不可提现余额支付
        tradeBill.tradeType = TradeType.WITHDRAW;                       //交易类型为提现
        tradeBill.amount = tradeBill.balancePaymentAmount
                .add(tradeBill.ebankPaymentAmount)
                .add(tradeBill.uncashPaymentAmount);
        tradeBill.withdrawBill = WithdrawBill.findById(withdrawBillId);

        return tradeBill.save();
    }

    /**
     * 创建退款交易记录.
     *
     * @param account 收款账户
     * @param amount  退款金额
     * @param orderId 关联的订单号
     * @return 退款交易
     */
    public static TradeBill createRefundTrade(Account account, BigDecimal amount, BigDecimal promotionAmount, Long orderId, String eCouponSn) {
        if (account == null) {
            throw new IllegalArgumentException("error while create refund trade: invalid account");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("error while create refund trade. invalid amount: " + amount);
        }
        if (promotionAmount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("error while create refund trade. invalid promotionAmount: " + promotionAmount);
        }

        if (orderId == null) {
            throw new IllegalArgumentException("error while create refund trade: invalid orderId");
        }

        TradeBill tradeBill = new TradeBill();
        tradeBill.fromAccount = AccountUtil.getPlatformIncomingAccount(); //付款方为平台收款账户
        tradeBill.toAccount = account;                                  //收款方为指定账户
        tradeBill.balancePaymentAmount = amount;                                   //使用可提现余额来支付退款的金额
        tradeBill.ebankPaymentAmount = BigDecimal.ZERO;                          //不使用网银支付
        tradeBill.uncashPaymentAmount = BigDecimal.ZERO;                          //不使用不可提现余额支付
        tradeBill.promotionPaymentAmount = promotionAmount;                          //使用活动金余额来支付退款的金额
        tradeBill.tradeType = TradeType.REFUND;                         //交易类型为退款
        tradeBill.orderId = orderId;                                  //冗余订单ID
        tradeBill.eCouponSn = eCouponSn;                                //冗余券编号
        tradeBill.amount = tradeBill.balancePaymentAmount
                .add(tradeBill.ebankPaymentAmount)
                .add(tradeBill.uncashPaymentAmount)
                .add(promotionAmount);

        return tradeBill.save();
    }

    /**
     * 创建转账交易记录.
     *
     * @param fromAccount  付款账户
     * @param toAccount    收款账户
     * @param cashAmount   可提现金额
     * @param uncashAmount 不可提现金额
     * @return 转账交易
     */
    public static TradeBill createTransferTrade(Account fromAccount, Account toAccount,
                                                BigDecimal cashAmount, BigDecimal uncashAmount) {
        if (fromAccount == null || toAccount == null) {
            throw new IllegalArgumentException("error while create transfer trade: invalid account");
        }
        if (cashAmount == null || cashAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("error while create transfer trade: invalid cashAmount: " + cashAmount);
        }
        if (uncashAmount == null || uncashAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("error while create transfer trade: invalid uncashAmount: " + uncashAmount);
        }

        TradeBill tradeBill = new TradeBill();
        tradeBill.fromAccount = fromAccount;                              //付款账户
        tradeBill.toAccount = toAccount;                                //收款账户
        tradeBill.balancePaymentAmount = cashAmount;                               //可提现余额支付金额
        tradeBill.ebankPaymentAmount = BigDecimal.ZERO;                          //不使用网银支付
        tradeBill.uncashPaymentAmount = uncashAmount;                             //不可提现余额支付金额
        tradeBill.tradeType = TradeType.TRANSFER;                       //交易类型为退款
        tradeBill.amount = tradeBill.balancePaymentAmount
                .add(tradeBill.ebankPaymentAmount)
                .add(tradeBill.uncashPaymentAmount);

        return tradeBill.save();
    }


    public static boolean success(TradeBill tradeBill, String note, String comment, String operatedBy) {
        if (tradeBill.fromAccount == null || tradeBill.toAccount == null) {
            throw new RuntimeException("invalid trade: none of the fromAccount or toAccount is available. trade:" + tradeBill.getId());
        }
        try {
            AccountUtil.addBalanceAndSaveSequence(
                    tradeBill.fromAccount.getId(),
                    tradeBill.balancePaymentAmount.add(tradeBill.ebankPaymentAmount).negate(),
                    tradeBill.uncashPaymentAmount.negate(),
                    tradeBill.promotionPaymentAmount.negate(),
                    tradeBill.getId(),
                    tradeBill.tradeType,
                    AccountSequenceFlag.NOSTRO,
                    note,
                    tradeBill.orderId, comment, operatedBy);

            AccountUtil.addBalanceAndSaveSequence(
                    tradeBill.toAccount.getId(),
                    tradeBill.ebankPaymentAmount.add(tradeBill.balancePaymentAmount),
                    tradeBill.uncashPaymentAmount,
                    tradeBill.promotionPaymentAmount,
                    tradeBill.getId(),
                    tradeBill.tradeType,
                    AccountSequenceFlag.VOSTRO,
                    note,
                    tradeBill.orderId, comment, operatedBy);
        } catch (BalanceNotEnoughException e) {
            Logger.error(e, e.getMessage());
            throw new RuntimeException("balance not enough, trade bill: " + tradeBill.getId(), e);
        } catch (AccountNotFoundException e) {
            Logger.error(e, e.getMessage());
            throw new RuntimeException("account not found, trade bill: " + tradeBill.getId(), e);
        }
        tradeBill.tradeStatus = TradeStatus.SUCCESS;
        tradeBill.save();
        return true;
    }


    /**
     * 交易成功
     *
     * @param tradeBill 需变更的交易记录
     * @return 是否成功
     */
    public static boolean success(TradeBill tradeBill, String note, SettlementStatus settlementStatus) {
        if (tradeBill.fromAccount == null || tradeBill.toAccount == null) {
            throw new RuntimeException("invalid trade: none of the fromAccount or toAccount is available. trade:" + tradeBill.getId());
        }
        try {
            AccountUtil.addBalanceAndSaveSequence(
                    tradeBill.fromAccount.getId(),
                    tradeBill.balancePaymentAmount.add(tradeBill.ebankPaymentAmount).negate(),
                    tradeBill.uncashPaymentAmount.negate(),
                    tradeBill.promotionPaymentAmount.negate(),
                    tradeBill.getId(),
                    tradeBill.tradeType,
                    AccountSequenceFlag.NOSTRO,
                    settlementStatus,
                    note,
                    tradeBill.orderId);

            AccountUtil.addBalanceAndSaveSequence(
                    tradeBill.toAccount.getId(),
                    tradeBill.ebankPaymentAmount.add(tradeBill.balancePaymentAmount),
                    tradeBill.uncashPaymentAmount,
                    tradeBill.promotionPaymentAmount,
                    tradeBill.getId(),
                    tradeBill.tradeType,
                    AccountSequenceFlag.VOSTRO,
                    settlementStatus,
                    note,
                    tradeBill.orderId);
        } catch (BalanceNotEnoughException e) {
            Logger.error(e, e.getMessage());
            throw new RuntimeException("balance not enough, trade bill: " + tradeBill.getId(), e);
        } catch (AccountNotFoundException e) {
            Logger.error(e, e.getMessage());
            throw new RuntimeException("account not found, trade bill: " + tradeBill.getId(), e);
        }
        tradeBill.tradeStatus = TradeStatus.SUCCESS;
        tradeBill.save();
        return true;
    }

    /**
     * 交易成功
     *
     * @param tradeBill 需变更的交易记录
     * @return 是否成功
     */
    public static boolean success(TradeBill tradeBill, String note) {
        return success(tradeBill, note, SettlementStatus.UNCLEARED);
    }
}
