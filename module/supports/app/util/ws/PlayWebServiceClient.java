package util.ws;

import models.journal.WebServiceCallLogData;
import org.apache.commons.lang.StringUtils;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;

import java.io.File;

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
    protected HttpResponse doHttpProcess(WebServiceRequest webServiceRequest, WebServiceCallLogData log,
                                         HttpMethod httpMethod) {
        WSRequest request = null;
        System.out.println("post encoding:" + encoding);
        if (encoding != null) {
            System.out.println("  do encoding...");
            request = WS.withEncoding(encoding).url(log.url);
        } else {
            request = WS.url(log.url);
        }

        if (!(httpMethod == HttpMethod.GET) && StringUtils.isNotBlank(log.requestBody)) {
            // 如果有requestBody，则不能使用params和uploadFile.
            request = request.body(log.requestBody);
        } else {
            if (webServiceRequest.params != null && webServiceRequest.params.size() > 0) {
                request = request.params(webServiceRequest.params);
            }
            if (webServiceRequest.uploadFiles != null && webServiceRequest.uploadFiles.size() > 0) {
                request = request.files(webServiceRequest.uploadFiles.toArray(new File[webServiceRequest.uploadFiles.size()]));
                log.setFilesName(webServiceRequest.uploadFiles);
            }
        }

        play.libs.WS.HttpResponse response;
        switch (httpMethod) {
            case GET:
                response = request.get(); break;
            case POST:
                response = request.post(); break;
            case PUT:
                response = request.put(); break;
            case DELETE:
                response = request.delete(); break;
            default:
                response = request.get();
        }
        log.statusCode = response.getStatus();
        log.responseText = response.getString();
        if (webServiceRequest.callback != null) {
            webServiceRequest.callback.process(response.getStatus(), response.getString());
        }
        return response;
    }

}
