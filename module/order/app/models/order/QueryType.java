package models.order;

/**
 * <p/>
 * User: yanjy
 * Date: 12-7-26
 * Time: 上午9:46
 */
public enum QueryType {
    GOODS_NAME,//商品名称
    ORDER_NUMBER,//订单编号
    LOGIN_NAME,//帐号
    MOBILE,//手机
    CLERK_JOB_NUMBER,//店员工号
    SHOP_NAME,//消费门店名称
    COUPON,
    ALLPHONE,  //按手机号码搜索
    UID,      //按用户Id搜索
    EXPRESS_NUMBER    //按物流单号搜索
}
