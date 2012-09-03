package models.job.yihaodian;

/**
 * @author likang
 *         Date: 12-8-30
 */
public enum OrderStatus {
    ORDER_WAIT_PAY,             //已下单（货款未全收）
    ORDER_PAYED,                //已下单（货款已收）
    ORDER_TRUNED_TO_DO,         //可以发货（已送仓库）
    ORDER_CAN_OUT_OF_WH,        //可出库
    ORDER_SENDED_TO_LOGITSIC,   //已发送物流
    ORDER_RECEIVED,             //货物用户已收到
    ORDER_FINISH,               //订单完成
    ORDER_CUSTOM_CALLTO_RETURN, //用户要求退货
    ORDER_CUSTOM_CALLTO_CHANGE, //用户要求换货
    ORDER_RETURNED,             //退货完成
    ORDER_CHANGE_FINISHED,      //换货完成
    ORDER_CANCEL;               //订单取消

    public static OrderStatus getStatus(String status){
        switch (status.toLowerCase()){
            case "order_wait_pay": return ORDER_WAIT_PAY;
            case "order_payed": return ORDER_PAYED;
            case "order_truned_to_do": return ORDER_TRUNED_TO_DO;
            case "order_can_out_of_wh": return ORDER_CAN_OUT_OF_WH;
            case "order_sended_to_logitsic": return ORDER_SENDED_TO_LOGITSIC;
            case "order_received": return ORDER_RECEIVED;
            case "order_finish": return ORDER_FINISH;
            case "order_custom_callto_return": return ORDER_CUSTOM_CALLTO_RETURN;
            case "order_custom_callto_change": return ORDER_CUSTOM_CALLTO_CHANGE;
            case "order_returned": return ORDER_RETURNED;
            case "order_change_finished": return ORDER_CHANGE_FINISHED;
            case "order_cancel": return ORDER_CANCEL;
        }
        return null;

    }

}
