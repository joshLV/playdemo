package util.mq;

public class MockMessage {
    
    public String queue;
    
    public Object message;
    
    public MockMessage(String queue2, Object message2) {
        this.queue = queue2;
        this.message = message2;
    }

}
