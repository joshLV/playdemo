package models.dangdang.groupbuy;

/**
 * 当当错误码.
 * <p/>
 * User: yanjy
 * Date: 12-9-15
 * Time: 下午7:20
 */
public enum DDErrorCode {

    SUCCESS(0),

    SIGN_ERROR(1000),//签名错误
    VERIFY_FAILED(1001),//验证失败
    USER_NOT_EXITED(1003), //用户不存在,可能是source_id或spgid输入错误（对于一百券则是传入的用户不存在或者没有对应的分销商）
    NO_DATA_NODE(1004),//没有数据节点

    ECOUPON_NOT_EXITED(3003),//序列号或验证码不存在
    ORDER_NOT_EXITED(9001),//订单不存在
    ORDER_EXCEPTION(9002),//订单异常
    PARSE_XML_FAILED(9003),//xml解析失败
    INVENTORY_NOT_ENOUGH(9004),//库存不足
    COUPON_SN_NOT_EXISTED(9005),//券号不存在
    MESSAGE_SEND_FAILED(9006),//短信发送失败
    COUPON_CONSUMED(9007),//券已消费
    COUPON_REFUND(9008),//券已退款
    COUPON_EXPIRED(9009);//券已过期

    private int value;

    DDErrorCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DDErrorCode getErrorCode(int code) {
        switch (code) {
            case 0:
                return SUCCESS;
            case 1000:
                return SIGN_ERROR;
            case 1001:
                return VERIFY_FAILED;
            case 1003:
                return USER_NOT_EXITED;
            case 1004:
                return NO_DATA_NODE;
            case 3003:
                return ECOUPON_NOT_EXITED;
            case 9001:
                return ORDER_NOT_EXITED;
            case 9002:
                return ORDER_EXCEPTION;
            case 9003:
                return PARSE_XML_FAILED;
            case 9004:
                return INVENTORY_NOT_ENOUGH;
            case 9005:
                return COUPON_SN_NOT_EXISTED;
            case 9006:
                return MESSAGE_SEND_FAILED;
            case 9007:
                return COUPON_CONSUMED;
            case 9008:
                return COUPON_REFUND;
            case 9009:
                return COUPON_EXPIRED;
            default:
                return VERIFY_FAILED;
        }
    }
}
