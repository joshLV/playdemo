package util.ws;

import models.journal.WebServiceCallLogData;
import org.apache.commons.io.input.ReaderInputStream;
import org.w3c.dom.Document;
import play.Logger;
import play.libs.WS.HttpResponse;
import play.mvc.Http.Header;
import play.vfs.VirtualFile;

import java.io.InputStream;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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

    /**
     * 测试环境都是UTF-8.
     * @param encoding
     * @return
     */
    public static WebServiceClient getInstance(String encoding) {
        MockWebServiceClient instance = new MockWebServiceClient();
        instance.encoding = encoding;
        return instance;
    }

    /**
     * 把响应内容content和状态码status加入队列，在下次MockWebServiceClient.get或post时返回内容。
     * 注意使用的是先进先出队列。
     * @param status 响应状态码
     * @param content 返回内容
     */
    public static void addMockHttpRequest(int status, String content) {
        _queueResponse.add(new MockHttpResponse(status, content));
    }

    /**
     * 把指定文件的内容放到响应队列中。
     * @param status 响应状态码
     * @param virtualFile 指定文件，文件内容将作为返回内容。
     */
    public static void addMockHttpRequestFromFile(int status, String virtualFile) {
        String content = VirtualFile.fromRelativePath(virtualFile).contentAsString();
        _queueResponse.add(new MockHttpResponse(status, content));
    }

    private final static Stack<WebServiceCallLogData> _stackCallLogs = new Stack<>();
    private final static Queue<MockHttpResponse> _queueResponse = new LinkedList<>();

    public static void clear() {
        _stackCallLogs.clear();
        _queueResponse.clear();
    }

    public static WebServiceCallLogData getLastWebServiceCallLog() {
        return _stackCallLogs.pop();
    }

    private static MockHttpResponse pollMockHttpResponse() {
        return _queueResponse.poll();
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

        // 测试时使encoding只使用UTF-8（不支持GBK)
        @Override
        public Document getXml(String encoding) {
            return super.getXml("UTF-8");
        }

        @Override
        public String getString(String encoding) {
            return super.getString("UTF-8");
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
    protected HttpResponse doHttpProcess(WebServiceRequest webServiceRequest, WebServiceCallLogData log, HttpMethod method) {
        MockHttpResponse response = pollMockHttpResponse();

        Logger.info("返回mock request(status:" + response.status + "):" + response.content);
        log.statusCode = response.status;
        log.responseText = response.content;

        if (webServiceRequest.callback != null) {
            webServiceRequest.callback.process(response.status, response.content);
        }

        _stackCallLogs.push(log);
        return response;
    }
}
