package models.dangdang;

/**
 * TODO.
 * <p/>
 * User: yanjy
 * Date: 12-9-15
 * Time: 下午7:20
 */
public enum ErrorCode {

    SUCCESS("0"),
    VERIFY_FAILD("1001"),//验证失败
    ORDER_NOT_EXITED("1002"),//订单不存在
    USER_NOT_EXITED("1003"), //用户不存在
    NO_DATA_NODE("1004"),//没有数据节点
    ORDER_EXITED("1005"),//订单已存在
    PARSE_XML_FAILD("1006"),//解析失败
    INVENTORY_NOT_ENOUGH("1007");//库存不足
    private String value;

    ErrorCode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
