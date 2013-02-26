package models.accounts;

/**
 * @author likang
 * Date: 12-3-6
 */
public enum WithdrawBillStatus {
    APPLIED,    //已提交 待审批
    SUCCESS,    //审批通过
    REJECTED    //审批被拒绝
}
