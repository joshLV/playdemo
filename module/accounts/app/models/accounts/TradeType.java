package models.accounts;

/**
 * @author likang
 */
public enum TradeType {
    CHARGE,             //充值
    WITHDRAW,           //提现
    PAY,                //支付
    REFUND,             //交易退款
    COMMISSION,         //佣金
    PURCHASE_COSTING,   //采购成本
    PREPAYMENT_SETTLED,  //预付款结算
    FREIGHT,            //运费
    TRANSFER,           //转账
    DEBT_COLLECTION,    //收账
    PAY_DEBT,           //付账
    BALANCE_BILL;       //冲正


    /**
     * 是否与订单相关的类型.
     *
     * @return 是否与订单相关的类型
     */
    public boolean isOrder() {
        return !(this.equals(CHARGE) || this.equals(WITHDRAW));
    }
}
