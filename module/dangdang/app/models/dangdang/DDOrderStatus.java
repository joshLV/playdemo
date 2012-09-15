package dangdang;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-15
 * Time: 下午2:22
 */
public enum DDOrderStatus {
    ORDER_ACCEPT,//已接到当当订单
    ORDER_SEND,//发送券短信
    ORDER_FINISH//该订单处理完毕（已通知完当当）

}
