package models.accounts;

/**
 * 账户更改类型
 *
 * User: likang
 */
public enum AccountChangeType {
    CHARGE,     //充值
    WITHDRAW,   //提现
    PAY,        //支付
    REFUND,     //交易退款
    CANCEL      //交易取消
}
