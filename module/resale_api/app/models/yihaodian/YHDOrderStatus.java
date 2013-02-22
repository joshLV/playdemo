package models.yihaodian;

/**
 * @author likang
 *         Date: 12-8-30
 */
public enum YHDOrderStatus {
    ORDER_WAIT_PAY,             //已下单（货款未全收）
    ORDER_PAYED,                //已下单（货款已收）
    ORDER_TRUNED_TO_DO,         //可以发货（已送仓库）
    ORDER_OUT_OF_WH,            //已出库（货在途）
    ORDER_CAN_OUT_OF_WH,        //可出库
    ORDER_SENDED_TO_LOGITSIC,   //已发送物流
    ORDER_RECEIVED,             //货物用户已收到
    ORDER_FINISH,               //订单完成
    ORDER_CUSTOM_CALLTO_RETURN, //用户要求退货
    ORDER_CUSTOM_CALLTO_CHANGE, //用户要求换货
    ORDER_RETURNED,             //退货完成
    ORDER_CHANGE_FINISHED,      //换货完成
    ORDER_CANCEL                //订单取消
}
