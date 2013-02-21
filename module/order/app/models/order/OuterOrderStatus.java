package models.order;

/**
 * @author likang
 *         Date: 12-9-18
 */
public enum OuterOrderStatus {
    /**
     * 修改 此状态内容前，请先find usage 尤其是看下 YHDGroupBuy.java 的 orderInform方法的最后状态判断
     */
    ORDER_COPY,
    ORDER_DONE,
    ORDER_SYNCED,

    REFUND_COPY,
    REFUND_DONE,
    REFUND_SYNCED,

    RESEND_COPY,
    RESEND_DONE,
    RESEND_SYNCED,

    ORDER_CANCELED,
    ORDER_IGNORE,
}
