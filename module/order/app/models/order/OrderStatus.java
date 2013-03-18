package models.order;

/**
 * 订单和订单项的状态，注意顺序是确定的，不可换
 */
public enum OrderStatus {
    UNPAID,//未付款
    PAID,//已付款未发货,电子券最终状态为PAID
    PREPARED, //代打包,只限实物订单项
    CANCELED,//交易关闭

    //订单明细的状态
    SENT,//已发送或已发货

    UPLOADED  //渠道运单已上传

}
