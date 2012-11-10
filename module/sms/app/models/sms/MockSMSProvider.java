package models.sms;

import java.util.Stack;

@Deprecated
public class MockSMSProvider {
    
    private final static Stack<SMSMessage> _stack = new Stack<>();

    private int send(SMSMessage message) {
        _stack.push(message);
        return 0;
    }

    private String getProviderName() {
        return "MockSMSProvider";
    }

    private static SMSMessage getLastSMSMessage() {
       return _stack.pop();
   }
}
