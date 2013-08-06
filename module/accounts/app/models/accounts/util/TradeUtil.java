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
import models.operator.Operator;
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
     * 创建订单交易
     */
    public static TradeBill orderTrade(Operator operator) {
        TradeBill bill = new TradeBill();
        bill.toAccount = AccountUtil.getPlatformIncomingAccount(operator); //默认收款账户为平台收款账户
        bill.tradeType = TradeType.PAY;
        return bill;
    }


    /**
     * 创建充值交易
     */
    public static TradeBill chargeTrade(PaymentSource source, Operator operator) {
        TradeBill bill = new TradeBill();
        bill.fromAccount = AccountUtil.getPaymentPartnerAccount(source.paymentCode, operator);
        bill.tradeType = TradeType.CHARGE;
        return bill;
    }


    /**
     * 创建活动金充值交易
     */
    public static TradeBill promotionChargeTrade(Account toAccount, BigDecimal amount) {
        TradeBill bill = new TradeBill();
        bill.fromAccount = AccountUtil.getPromotionAccount(Operator.defaultOperator()); //只有视惠有
        bill.toAccount = toAccount;
        bill.tradeType = TradeType.CHARGE;
        return bill.promotionPaymentAmount(amount);
    }

    /**
     * 创建消费交易
     */
    public static TradeBill consumeTrade(Operator operator) {
        TradeBill bill = new TradeBill();
        bill.fromAccount = AccountUtil.getPlatformIncomingAccount(operator); //默认收款账户为平台收款账户
        bill.tradeType = TradeType.PURCHASE_COSTING;
        return bill;
    }

    /**
     * 创建佣金交易，消费者消费成功后，将佣金付给平台和一百券
     */
    public static TradeBill commissionTrade(Operator operator) {
        TradeBill bill = new TradeBill();
        bill.fromAccount = AccountUtil.getPlatformIncomingAccount(operator);  //付款方账户为平台收款账户
        bill.tradeType = TradeType.COMMISSION;
        return bill;
    }

    public static TradeBill commissionTrade(Account account) {
        TradeBill bill = new TradeBill();
        bill.fromAccount = account;  //付款方账户为平台收款账户
        bill.tradeType = TradeType.COMMISSION;
        return bill;
    }

    /**
     * 创建运费交易，发货后,平台佣金账户将收取运费
     */
    public static TradeBill freightTrade(Operator operator) {
        TradeBill bill = new TradeBill();
        bill.fromAccount = AccountUtil.getPlatformIncomingAccount(operator);
        bill.tradeType = TradeType.FREIGHT;
        return bill;
    }

    /**
     * 创建提现交易,提现成功后,账户的不可用余额将减少
     * 注意,只有在提现审批通过时才有必要创建此trade,申请和拒绝时不必创建
     */
    public static TradeBill withdrawTrade(Account fromAccount, BigDecimal amount, Long withdrawBillId) {
        return withdrawTrade(fromAccount, amount, withdrawBillId, Operator.defaultOperator());
    }

    public static TradeBill withdrawTrade(Account fromAccount, BigDecimal amount, Long withdrawBillId, Operator operator) {
        TradeBill bill = new TradeBill();
        bill.fromAccount = fromAccount;
        bill.toAccount = AccountUtil.getPlatformWithdrawAccount(operator);
        bill.uncashPaymentAmount(amount);
        bill.withdrawBill = WithdrawBill.findById(withdrawBillId);
        bill.tradeType = TradeType.WITHDRAW;
        return bill;
    }

    /**
     * 创建退款交易记录.
     * 付款方为平台收款账户.
     */
    public static TradeBill refundFromPlatFormIncomingTrade(Operator operator) {
        TradeBill bill = new TradeBill();
        bill.fromAccount = AccountUtil.getPlatformIncomingAccount(operator); //付款方为平台收款账户
        bill.tradeType = TradeType.REFUND;
        return bill;
    }

    /**
     * 创建退款交易记录.
     * 收款方为平台收款账户
     */
    public static TradeBill refundToPlatFormIncomingTrade(Operator operator) {
        TradeBill bill = new TradeBill();
        bill.toAccount = AccountUtil.getPlatformIncomingAccount(operator); //收款方为平台收款账户
        bill.tradeType = TradeType.REFUND;
        return bill;
    }

    /**
     * 创建冲正交易
     */
    public static TradeBill balanceBill(Account fromAccount, Account toAccount, TradeType tradeType,
                                        BigDecimal amount, Long orderId) {
        TradeBill bill = new TradeBill();
        bill.fromAccount = fromAccount;
        bill.toAccount = toAccount;
        bill.tradeType = tradeType;
        bill.amount = amount;
        bill.balancePaymentAmount = amount;
        bill.orderId = orderId;
        return bill;
    }

    /**
     * 创建退款交易
     */
    public static TradeBill refundTrade() {
        TradeBill bill = new TradeBill();
        bill.tradeType = TradeType.REFUND;
        return bill;
    }

    /**
     * 创建转账交易
     */
    public static TradeBill transferTrade() {
        TradeBill bill = new TradeBill();
        bill.tradeType = TradeType.TRANSFER;
        return bill;
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
        if (tradeBill.id == null) {
            throw new RuntimeException("you must save the trade bill before you call this function");
        }
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
