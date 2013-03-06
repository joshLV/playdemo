package util.extension;

/**
 * 用于ExtensionInvoker.run方法的默认动作。
 */
public interface DefaultAction<T extends InvocationContext> {
    /**
     * 默认操作，用于在ExtensionHandler中包装默认操作值.
     * @return
     */
    ExtensionResult execute(T context);
}
