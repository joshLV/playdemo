package util.ws;

import java.util.Stack;

import models.journal.WebServiceCallLog;

public class MockWebServiceClientHelper extends WebServiceClientHelper {

    static MockWebServiceClientHelper _instance;
    
    private MockWebServiceClientHelper() {
        // 单例
    }
    
    public static MockWebServiceClientHelper getInstance() {
        if (_instance == null) {
            _instance = new MockWebServiceClientHelper();
        }
        return _instance;
    }
    
    
    private String responseContent;
    private int statusCode;
    
    public void setResponseContent(String content) {
        responseContent = content;
    }
    
    public void setStatusCode(int code) {
        statusCode = code;
    }
    
    @Override
    public void doGet(WebServiceCallLog log, WebServiceCallback callback) {
        _stack.push(log);
        log.statusCode = this.statusCode;
        log.responseText = this.responseContent;
        callback.process(statusCode, responseContent);
    }

    private final static Stack<WebServiceCallLog> _stack = new Stack<>();

    public static WebServiceCallLog getLastWebServiceCallLog() {
        return _stack.pop();
    }

}
