package models.accounts.util;

import models.accounts.AccountSequenceType;
import models.accounts.RefundBill;
import models.accounts.RefundStatus;
import models.accounts.TradeBill;
import play.Logger;

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
     * @param applyNote     退款申请
     * @param cashAmount    可提现金额
     * @param uncashAmount  不可提现金额
     * @return              创建成功的退款流水
     */
    public static RefundBill create(TradeBill tradeBill, Long orderId, String applyNote,
                                    BigDecimal cashAmount, BigDecimal uncashAmount){
        if(tradeBill == null){
            Logger.error("error while create refund bill: invalid tradeBill");
            return null;
        }
        if(orderId == null){
            Logger.error("error while create refund bill: invalid orderId");
            return null;
        }

        return new RefundBill(tradeBill, orderId, applyNote, cashAmount,uncashAmount)
                .save();

    }

    /**
     * 退款申请成功.
     *
     * @param refundBill    原退款申请流水
     * @return              修改状态后的退款流水
     */
    public static RefundBill success(RefundBill refundBill){
        refundBill.refundStatus = RefundStatus.SUCCESS;

        //更新账户余额
        AccountUtil.addCash(refundBill.account, refundBill.cashAmount,refundBill.getId(),
                AccountSequenceType.REFUND,"退款");

        if(refundBill.uncashAmount.compareTo(BigDecimal.ZERO) > 0){
            AccountUtil.addUncash(refundBill.account, refundBill.uncashAmount, refundBill.getId(),
                    AccountSequenceType.REFUND, "退款");
        }

        return refundBill.save();
    }

}
