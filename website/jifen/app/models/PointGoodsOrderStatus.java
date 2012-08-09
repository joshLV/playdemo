package models;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-8
 * Time: 下午2:35
 * To change this template use File | Settings | File Templates.
 */
public enum PointGoodsOrderStatus {
    APPLY,      //订单提交待审核
    ACCEPT,     //审核通过待发货
    CANCELED,   //审核未通过交易关闭
    SENT        //已发送或已发货

}
