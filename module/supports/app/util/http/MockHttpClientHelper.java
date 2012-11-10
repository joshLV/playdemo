package util.http;

import java.util.Stack;

public class MockHttpClientHelper implements HttpClientHelper {

    static MockHttpClientHelper _instance;
    
    private MockHttpClientHelper() {
        // 单例
    }
    
    public static MockHttpClientHelper getInstance() {
        if (_instance == null) {
            _instance = new MockHttpClientHelper();
        }
        return _instance;
    }
    
    
    private String returnContent;
    private int statusCode;
    
    public void setReturnContent(String content) {
        returnContent = content;
    }
    
    public void setStatusCode(int code) {
        statusCode = code;
    }
    
    @Override
    public void processGetUrl(String url, HttpCallback callback) {
        _stackUrl.push(url);
        callback.process(statusCode, returnContent);
    }

    private final static Stack<String> _stackUrl = new Stack<>();

    public static String getLastUrl() {
        return _stackUrl.pop();
    }

}
