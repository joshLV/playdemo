package models.yihaodian.groupbuy;

/**
 * @author likang
 *         Date: 12-9-12
 */
public enum YHDGroupBuyOrderJobFlag {
    ORDER_COPY,         // 从一号店获知订单信息
    ORDER_DONE,         // 已发货
    ORDER_SYNCED,       // 已通知一号店发货完成

    REFUND_COPY,        // 从一号店获知退款信息
    REFUND_DONE,        // 退款已完成
    REFUND_SYNCED       // 已通知一号店退款完成
}
