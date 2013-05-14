package models.order;

/**
 * User: tanglq
 * Date: 13-5-13
 * Time: 上午11:12
 */
public enum OrderItemsFeeType {
    SMS_ECOUPON,         //发券短信费
    SMS_VERIFY_NOTIFY,   //验证成功提醒
    SMS_EXPIRE_NOTIFY,   //券过期提醒
    PHONE_VERFIY         //电话验证费用
}
