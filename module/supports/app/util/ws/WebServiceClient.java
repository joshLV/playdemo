package util.ws;

import com.google.gson.Gson;
import models.journal.WebServiceCallLogData;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.libs.WS.HttpResponse;
import util.mq.MQPublisher;

public abstract class WebServiceClient {

    public static final String MQ_KEY = "ws.call-log";


    public enum HttpMethod {
        GET, POST, PUT, DELETE
    }

    public String encoding;

    public HttpResponse processHttpResponse(WebServiceRequest webServiceRequest, HttpMethod method) {
        Logger.info("call " + webServiceRequest.callType + "'s " + method + "(" + webServiceRequest.url + ")...");
        WebServiceCallLogData log = createWebServiceCallLog(webServiceRequest.callType, method, webServiceRequest.url,
                webServiceRequest.keyword1,
                webServiceRequest.keyword2, webServiceRequest.keyword3);
        log.requestBody = webServiceRequest.requestBody;
        if (webServiceRequest.params != null) {
            log.postParams = new Gson().toJson(webServiceRequest.params);
        }

        long startTime = System.currentTimeMillis();
        try {
            HttpResponse response = doHttpProcess(webServiceRequest, log, method);
            long endTime = System.currentTimeMillis();
            log.duration = endTime - startTime; // 记录耗时
            log.responseText = response.getString();
            log.statusCode = response.getStatus();
            log.success = Boolean.TRUE;
            if (StringUtils.isNotBlank(webServiceRequest.callType)) {
//                sendLogToMQ(log);
            }
            return response;
        } catch (Exception e) {
            Logger.error("postHttpResponse(callType:" + webServiceRequest.callType + ", url:" + webServiceRequest.url + "...) exception:" +
                    e.getMessage(), e);
            if (StringUtils.isNotBlank(webServiceRequest.callType)) {
                long endTime = System.currentTimeMillis();
                log.duration = endTime - startTime; // 记录耗时
                log.exceptionText = e.getMessage();
                log.statusCode = -1;
                log.success = Boolean.FALSE;
//                sendLogToMQ(log);
            }

            throw e;
        }

    }

    protected WebServiceCallLogData createWebServiceCallLog(String callType, HttpMethod callMethod, String url,
                                                         String keyword1, String keyword2, String keyword3) {
        WebServiceCallLogData log = new WebServiceCallLogData();

        log.callType = callType;
        log.callMethod = callMethod.toString();
        log.key1 = keyword1;
        log.key2 = keyword2;
        log.key3 = keyword3;
        log.url = url;
        return log;
    }

    protected abstract HttpResponse doHttpProcess( WebServiceRequest webServiceRequest, WebServiceCallLogData log,
                                                   HttpMethod method);

    protected void sendLogToMQ(WebServiceCallLogData log) {
        MQPublisher.publish(MQ_KEY, log);
    }
}
