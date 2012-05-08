package models.sms;

public class SMSException extends RuntimeException {
    
    int _resultCode;

    public SMSException(int resultCode, String message, Throwable cause) {
        super(message, cause);
        _resultCode = resultCode;
    }

    public SMSException(int resultCode, String message) {
        super(message);
        _resultCode = resultCode;
    }

    public SMSException(int resultCode, Throwable cause) {
        super(cause);
        _resultCode = resultCode;
    }

    public int getResultCode() {
        return _resultCode;
    }
    
}
