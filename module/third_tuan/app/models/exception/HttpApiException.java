package models.exception;

public class HttpApiException extends RuntimeException {

    int _resultCode;

    public HttpApiException(int resultCode, String message, Throwable cause) {
        super(message, cause);
        _resultCode = resultCode;
    }

    public HttpApiException(int resultCode, String message) {
        super(message);
        _resultCode = resultCode;
    }

    public HttpApiException(int resultCode, Throwable cause) {
        super(cause);
        _resultCode = resultCode;
    }

    public int getResultCode() {
        return _resultCode;
    }
}
