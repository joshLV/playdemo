package models.dangdang;

/**
 * 当当错误码.
 * <p/>
 * User: yanjy
 * Date: 12-9-15
 * Time: 下午7:20
 */
public enum ErrorCode {

    SUCCESS(0),
    SIGN_ERROR(1000),//签名错误
    VERIFY_FAILED(1001),//验证失败
    ORDER_NOT_EXITED(1002),//订单不存在
    USER_NOT_EXITED(1003), //用户不存在
    NO_DATA_NODE(1004),//没有数据节点
    ORDER_EXITED(1005),//订单已存在
    PARSE_XML_FAILED(1006),//解析失败

    INVENTORY_NOT_ENOUGH(1007),//库存不足
    COUPON_SN_NOT_EXISTED(1008),//券号不存在
    MESSAGE_SEND_FAILED(1009);//短信发送失败
    private int value;

    ErrorCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ErrorCode getErrorCode(int code) {
        switch (code) {
            case 0:
                return SUCCESS;
            case 1000:
                return SIGN_ERROR;
            case 1001:
                return VERIFY_FAILED;
            case 1002:
                return ORDER_NOT_EXITED;
            case 1003:
                return USER_NOT_EXITED;
            case 1004:
                return NO_DATA_NODE;
            case 1005:
                return ORDER_EXITED;
            case 1006:
                return PARSE_XML_FAILED;
            case 1007:
                return INVENTORY_NOT_ENOUGH;
            default:
                return VERIFY_FAILED;
        }
    }
}
