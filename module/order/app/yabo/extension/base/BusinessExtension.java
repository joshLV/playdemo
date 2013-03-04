package yabo.extension.base;

/**
 * 业务扩展统一抽象接口.
 *
 * 具体业务扩展需要实现此接口，并定义自己的业务扩展方法。
 *
 * 我们约定，
 */
public interface BusinessExtension<T extends BusinessContext> {

    ExtensionResult execute(T context);

    /**
     * 检查此扩展点是否适用于对应的context
     * @param context
     * @return 如果返回True，表示适用，会
     */
    boolean canUse(T context);
}
