package facade.order.translate;

/**
 * User: tanglq
 * Date: 13-6-20
 * Time: 上午11:20
 */
public class JDOrderMessage extends OuterOrderMessage {
    public Integer code;
    public String message;

    public JDOrderMessage(Integer _code, String _message) {
        this.code = _code;
        this.message = _message;
    }
}
