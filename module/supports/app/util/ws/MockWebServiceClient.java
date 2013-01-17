package util.ws;

import models.journal.WebServiceCallLogData;
import org.apache.commons.io.input.ReaderInputStream;
import play.Logger;
import play.libs.WS.HttpResponse;
import play.mvc.Http.Header;

import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class MockWebServiceClient extends WebServiceClient {

    static MockWebServiceClient _instance;
    
    private MockWebServiceClient() {
        // 单例
    }
    
    public static MockWebServiceClient getInstance() {
        if (_instance == null) {
            _instance = new MockWebServiceClient();
        }
        return _instance;
    }
    
    
    public static void pushMockHttpRequest(int status, String content) {
        _stackResponse.push(new MockHttpResponse(status, content));
    }
    
    @Override
    public HttpResponse doGet(WebServiceCallLogData log, WebServiceCallback callback) {
        MockHttpResponse response = popMockHttpResponse();
        
        log.statusCode = response.status;
        log.responseText = response.content;
        if (callback != null) {
            callback.process(response.status, response.content);
        }
        
        _stack.push(log);        
        return response;
    }

    private final static Stack<WebServiceCallLogData> _stack = new Stack<>();
    private final static Stack<MockHttpResponse> _stackResponse = new Stack<>();
    
    public static void clear() {
        _stack.clear();
        _stackResponse.clear();
    }
    
    public static WebServiceCallLogData getLastWebServiceCallLog() {
        return _stack.pop();
    }
    
    public static MockHttpResponse popMockHttpResponse() {
        return _stackResponse.pop();
    }

    public static class MockHttpResponse extends HttpResponse {
        
        public MockHttpResponse(int status, String content) {
            this.status = status;
            this.content = content;
        }
        
        public String content;
        
        public int status;
        
        @Override
        public InputStream getStream() {
            return new ReaderInputStream(new StringReader(content));
        }
        
        @Override
        public Integer getStatus() {
            return status;
        }
        
        @Override
        public List<Header> getHeaders() {
            return null;
        }
        
        @Override
        public String getHeader(String key) {
            return null;
        }

        public String getStatusText() {
            return status + ":OK";
        }
    }

    @Override
    protected HttpResponse doPost(WebServiceCallLogData log,
                    Map<String, Object> params, WebServiceCallback callback) {
        MockHttpResponse response = popMockHttpResponse();
        
        Logger.info("返回mock request(status:" + response.status + "):" + response.content);
        log.statusCode = response.status;
        log.responseText = response.content;
        
        if (callback != null) {
            callback.process(response.status, response.content);
        }
        
        _stack.push(log);        
        return response;
    };
}
