package facade.order.translate;

/**
 * 京东生成订单的返回消息包装类.
 */
public class JDOrderMessage extends OuterOrderMessage {
    public Integer code;
    public String message;

    public JDOrderMessage(Integer _code, String _message) {
        this.code = _code;
        this.message = _message;
    }
}
