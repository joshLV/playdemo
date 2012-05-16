package models.accounts.util;

import models.accounts.*;
import play.Logger;
import play.db.jpa.JPAPlugin;

import java.math.BigDecimal;

/**
 * 退款流水工具.
 *
 * @author likang
 * Date: 12-3-7
 */
public class RefundUtil {

    /**
     * 创建退款流水.
     *
     * @param tradeBill     原交易流水
     * @param orderId       原订单号
     * @param orderItemId   申请退款的订单条目
     * @param amount        退款金额
     * @return              创建成功的退款流水
     */
    public static RefundBill create(TradeBill tradeBill, Long orderId, Long orderItemId, BigDecimal amount,
                                    String applyNote){
        if(tradeBill == null){
            Logger.error("error while create refund bill: invalid tradeBill");
            return null;
        }
        if(tradeBill.tradeStatus != TradeStatus.SUCCESS) {
            Logger.error("error while create refund bill: tradebill is not paid");
            return null;
        }
        if(orderId == null){
            Logger.error("error while create refund bill: invalid orderId");
            return null;
        }

        return new RefundBill(tradeBill, orderId, orderItemId, amount, applyNote).save();

    }

    /**
     * 退款申请成功.
     *
     * @param refundBill    原退款申请流水
     * @return              是否成功
     */
    public static boolean success(RefundBill refundBill){
        refundBill.refundStatus = RefundStatus.SUCCESS;

        //更新账户余额
        try {
            AccountUtil.addBalance(refundBill.account.getId(), refundBill.amount, BigDecimal.ZERO, refundBill.getId(),
                    AccountSequenceType.REFUND, "退款", refundBill.orderId);
            AccountUtil.addBalance(AccountUtil.getPlatformIncomingAccount().getId(), refundBill.amount.negate(),
                    BigDecimal.ZERO, refundBill.getId(), AccountSequenceType.REFUND, "支付退款", refundBill.orderId);
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
        refundBill.save();
        return true;
    }

}
