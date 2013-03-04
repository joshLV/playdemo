package yabo.extension.base;

/**
 * 业务扩展返回值。
 *
 * 如果code为0,表示成功；其它值通过message描述.
 */
public class ExtensionResult {

    public int code;

    public String message;
}
