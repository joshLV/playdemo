package models.order;

/**
 * User: hejun
 * Date: 12-8-8
 * Time: 下午2:35
 */
public enum PointGoodsOrderStatus {
    APPLY,      //订单提交待审核
    ACCEPT,     //审核通过
    CANCELED,   //审核未通过
    SENT,
    UNSENT

}
