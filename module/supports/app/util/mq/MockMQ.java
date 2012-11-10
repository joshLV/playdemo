package util.mq;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.junit.Assert;

/**
 * 用于模拟消息队列.
 * @author tanglq
 */
public class MockMQ {

    public static final Map<String, Stack<MockMessage>> _messageQueue = new HashMap<>();
    
    public static void publish(String queue, Object message) {
        Stack<MockMessage> _stack = _messageQueue.get(queue);
        if (_stack == null) {
            _stack = new Stack<>();
            _messageQueue.put(queue, _stack);
        }
        _stack.push(new MockMessage(queue, message));
    }
    
    /**
     * 得到指定队列最后放进去的消息体.
     * @param queue
     * @return
     */
    public static Object getLastMessage(String queue) {
        Stack<MockMessage> _stack = _messageQueue.get(queue);
        if (_stack == null) {
            _stack = new Stack<>();
            _messageQueue.put(queue, _stack);
        }
        if (_stack.size() == 0) {
            //Assert.fail("NOT FOUND Any Message in Queue(" + queue + ")!");
            throw new RuntimeException("NOT found any message.");
            //return null;
        }
        MockMessage m = _stack.pop();
        return m.message;
    }
    
    public static void clear(String queue) {
        Stack<MockMessage> _stack = _messageQueue.get(queue);
        if (_stack == null) {
            _stack = new Stack<>();
            _messageQueue.put(queue, _stack);
        }
        _stack.clear();
    }
    
    public static void clear() {
        for (Stack stack : _messageQueue.values()) {
            stack.clear();
        }
        _messageQueue.clear();
    }
    
    public static int size(String queue) {
        Stack<MockMessage> _stack = _messageQueue.get(queue);
        if (_stack == null) {
            _stack = new Stack<>();
            _messageQueue.put(queue, _stack);
        }
        return _stack.size();
    }
}
