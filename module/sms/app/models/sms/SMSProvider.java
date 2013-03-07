package models.sms;

public interface SMSProvider {
    public void send(SMSMessage message);
}
