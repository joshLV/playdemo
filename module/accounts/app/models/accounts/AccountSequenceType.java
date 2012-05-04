package models.accounts;


/**
 * @author likang
 *         Date: 12-3-6
 */
public enum AccountSequenceType {
    CHARGE,     //充值
    WITHDRAW,   //提现
    PAY,        //支付
    REFUND,     //交易退款
    CANCEL,     //交易取消
    RECEIVE;     //收款

    /**
     * 是否与订单相关的类型.
     *
     * @return
     */
    public boolean isOrder() {
        return (this.equals(PAY) || this.equals(REFUND) || this.equals(CANCEL));
    }
}
