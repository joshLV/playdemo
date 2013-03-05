package yabo.extension.base;

/**
 * 业务扩展返回值。
 *
 * 如果code为0,表示成功；其它值通过message描述.
 */
public class ExtensionResult {

    public int code;

    public String message;

    public static ExtensionResult build() {
        return new ExtensionResult();
    }

    public ExtensionResult code(int value) {
        this.code = value;
        return this;
    }

    public ExtensionResult message(String value) {
        this.message = value;
        return this;
    }
}
