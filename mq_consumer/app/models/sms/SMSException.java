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

    public SMSException(String message, Throwable cause) {
        super(message, cause);
    }

    public SMSException(String message) {
        super(message);
    }

    public SMSException(Throwable cause) {
        super(cause);
    }

    public int getResultCode() {
        return _resultCode;
    }
    
}
