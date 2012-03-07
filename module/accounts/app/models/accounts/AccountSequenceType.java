package models.accounts;


/**
 * @author likang
 * Date: 12-3-6
 */
public enum AccountSequenceType {
    CHARGE,     //充值
    WITHDRAW,   //提现
    PAY,        //支付
    REFUND,     //交易退款
    CANCEL,     //交易取消
    RECEIVE     //收款
}
