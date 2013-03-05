package util.extension;

/**
 * User: tanglq
 * Date: 13-3-5
 * Time: 上午11:07
 */
public interface DefaultAction {
    /**
     * 默认操作，用于在ExtensionHandler中包装默认操作值.
     * @return
     */
    ExtensionResult execute(InvocationContext context);
}
