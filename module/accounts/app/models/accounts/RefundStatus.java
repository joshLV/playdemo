package models.accounts;

/**
 * User: likang
 * Date: 12-3-5
 */
public enum RefundStatus {
    APPLIED,        //已提交申请
    PROCESSING,     //处理中
    REJECTED,       //已拒绝
    SUCCESS,        //成功
    FAILED          //失败
}
