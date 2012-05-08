package models.sms;

import java.util.Stack;

public class MockSMSProvider implements SMSProvider {
    
    private final static Stack<SMSMessage> _stack = new Stack<>();

    @Override
    public int send(SMSMessage message) {
        _stack.push(message);
        return 0;
    }

    @Override
    public String getProviderName() {
        return "MockSMSProvider";
    }

   public static SMSMessage getLastSMSMessage() {
       return _stack.pop();
   }
}
