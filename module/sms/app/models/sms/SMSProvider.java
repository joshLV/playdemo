package models.sms;

public interface SMSProvider {
    public int send(SMSMessage message);
    
    public String getProviderName();
}
