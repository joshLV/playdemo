package util.extension;

/**
 * 业务扩展返回值。
 *
 * 如果code为0,表示成功；其它值通过message描述.
 */
public class ExtensionResult {

    public static ExtensionResult SUCCESS = ExtensionResult.code(0).message("Success");
    public static ExtensionResult INVALID_CALL = ExtensionResult.code(1).message("Invalid Call");

    public int code;

    public String message;

    private ExtensionResult(int value) {
        this.code = value;
    }

    public static ExtensionResult code(int value) {
        return new ExtensionResult(value);
    }

    public ExtensionResult message(String value) {
        this.message = value;
        return this;
    }

    public ExtensionResult message(String format, Object... values) {
        this.message = String.format(format, values);
        return this;
    }

    @Override
    public String toString() {
        return String.format("Result[%d: %s]", this.code, this.message);
    }
}
