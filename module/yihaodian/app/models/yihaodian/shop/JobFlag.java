package models.yihaodian.shop;

/**
 * @author likang
 *         Date: 12-9-3
 */
public enum JobFlag {
    SEND_COPY,      //发货任务已接收
    SEND_DONE,      //发货任务已处理
    SEND_SYNCED,    //发货任务已同步

    REFUND_COPY,    //退款任务已接收
    REFUND_DONE,    //退款任务已处理
    REFUND_SYNCED,  //退款任务已同步

    CANCEL_COPY,    //取消任务已接收
    CANCEL_DONE,    //取消任务已处理
    CANCEL_SYNCED,  //取消任务已同步

    IGNORE,         //订单中有实体券 忽略
}
