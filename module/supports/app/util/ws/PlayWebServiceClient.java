package util.ws;

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

    public static WebServiceClient getInstance(String encoding) {
        PlayWebServiceClient instance = new PlayWebServiceClient();
        instance.encoding = encoding;
        return instance;
    }

    private PlayWebServiceClient() {
    }

    @Override
    public HttpResponse doGet(WebServiceCallLogData log, WebServiceCallback callback) {
        WSRequest wsRequest = null;
        System.out.println("encoding:" + encoding);
        if (encoding != null) {
            System.out.println("do encoding.");
            wsRequest = WS.withEncoding(encoding).url(log.url);
        } else {
            wsRequest = WS.url(log.url);
        }

        play.libs.WS.HttpResponse response = wsRequest.get();
        log.statusCode = response.getStatus();
        log.responseText = response.getString();
        if (callback != null) {
            callback.process(response.getStatus(), response.getString());
        }
        return response;
    }

    @Override
    protected HttpResponse doPost(WebServiceCallLogData log, Map<String, Object> params, WebServiceCallback callback) {
        WSRequest request = null;
        System.out.println("post encoding:" + encoding);
        if (encoding != null) {
            System.out.println("  do encoding...");
            request = WS.withEncoding(encoding).url(log.url);
        } else {
            request = WS.url(log.url);
        }
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
