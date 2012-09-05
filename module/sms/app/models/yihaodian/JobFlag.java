package models.yihaodian;

/**
 * @author likang
 *         Date: 12-9-3
 */
public enum JobFlag {
    SEND_COPY,       //发货任务已接收
    SEND_DONE,       //发货任务已处理
    SEND_SYNCED,     //发货任务已同步

    REFUND_COPY,     //退款任务已接收
    REFUND_DONE,     //退款任务已处理
    REFUND_SYNCED    //退款任务已同步
}
