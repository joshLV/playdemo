package models.accounts;

/**
 * 账户更改类型
 *
 * User: likang
 */
public enum AccountChangeType {
    /*
    充值 02 支付 03 提现
    ￼￼￼￼￼￼￼￼￼￼￼￼￼￼Cust_Name
                  VARCHAR2(100      N
                           )
    ￼￼￼￼￼￼￼￼￼￼￼￼￼￼SeqFlag Char(1) N
    Change_Type Char(2) N
    PreAmount NUMBER(15,2) N
    Amount NUMBER(15,2) N
    Cash_Amount NUMBER(15,2) N
    变动前总 金额
    变动后总 金额
    可提现发
    04 内部调账 05 结息
    06 利息税
    07 原交易退款 08 原交易撤销
    */
    CHARGE,     //充值
    WITHDRAW,   //提现
    PAY,        //支付
    REFUND,     //交易退款
    CANCEL      //交易取消
}
