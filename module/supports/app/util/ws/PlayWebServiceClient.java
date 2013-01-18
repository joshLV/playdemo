package util.ws;

import models.journal.WebServiceCallLog;
import models.journal.WebServiceCallLogData;
import org.apache.commons.lang.StringUtils;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;

import java.util.Map;

public class PlayWebServiceClient extends WebServiceClient {

    private static PlayWebServiceClient _instance;

    public static PlayWebServiceClient getInstance() {
        if (_instance == null) {
            _instance = new PlayWebServiceClient();
        }
        return _instance;
    }

    private PlayWebServiceClient() {
    }

    @Override
    public HttpResponse doGet(WebServiceCallLogData log, WebServiceCallback callback) {
        play.libs.WS.HttpResponse response = WS.url(log.url).get();
        log.statusCode = response.getStatus();
        log.responseText = response.getString();
        if (callback != null) {
            callback.process(response.getStatus(), response.getString());
        }
        return response;
    }

    public void doPost(WebServiceCallLog log, WebServiceCallback callback) {

        WS.url("").post();
    }

    @Override
    protected HttpResponse doPost(WebServiceCallLogData log, Map<String, Object> params, WebServiceCallback callback) {
        WSRequest request = WS.url(log.url);
        if (params != null && params.size() > 0) {
            request = request.params(params);
        }
        if (StringUtils.isNotBlank(log.requestBody)) {
            request = request.body(log.requestBody);
        }
        play.libs.WS.HttpResponse response = request.post();
        log.statusCode = response.getStatus();
        log.responseText = response.getString();
        if (callback != null) {
            callback.process(response.getStatus(), response.getString());
        }
        return response;
    }
}
