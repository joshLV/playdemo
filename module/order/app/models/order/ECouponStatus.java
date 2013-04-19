package models.order;

/**
 * User: pwg
 * Date: 12-3-5
 */
public enum ECouponStatus {
    UNCONSUMED, // 未消费
    CONSUMED,   //已消费
    REFUND     //已退款

    // TODO: 考虑加上『已使用退款』，与『未使用退款』分开，以及『未启用』作为初始状态
}
