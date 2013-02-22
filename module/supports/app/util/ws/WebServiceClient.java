package util.ws;

import com.google.gson.Gson;
import models.journal.WebServiceCallLogData;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.libs.WS.HttpResponse;
import util.mq.MQPublisher;

public abstract class WebServiceClient {

    public static final String MQ_KEY = "ws.call-log";

    public String encoding;

    public HttpResponse getHttpResponse(WebServiceRequest webServiceRequest) {
        // 考虑到在MQ调用时没有打开数据库连接，这里重新开一下
        Logger.info("call " + webServiceRequest.callType + "'s get(" + webServiceRequest.url + ")...");
        WebServiceCallLogData log = createWebServiceCallLog(webServiceRequest.callType, "GET", webServiceRequest.url, webServiceRequest.keyword1, webServiceRequest.keyword2, webServiceRequest.keyword3);

        long startTime = System.currentTimeMillis();
        try {
            HttpResponse response = doGet(webServiceRequest, log);
            long endTime = System.currentTimeMillis();
            log.duration = endTime - startTime; // 记录耗时
            log.responseText = response.getString();
            log.statusCode = response.getStatus();
            log.success = Boolean.TRUE;
            if (StringUtils.isNotBlank(webServiceRequest.callType)) {
                sendLogToMQ(log);
            }
            return response;
        } catch (Exception e) {
            Logger.error("getHttpResponse(callType:" + webServiceRequest.callType + ", url:" + webServiceRequest.url + "...) exception:" +
                    e.getMessage(), e);
            if (StringUtils.isNotBlank(webServiceRequest.callType)) {
                long endTime = System.currentTimeMillis();
                log.duration = endTime - startTime; // 记录耗时
                log.exceptionText = e.getMessage();
                log.statusCode = -1;
                log.success = Boolean.FALSE;
                sendLogToMQ(log);
            }
            throw e;
        }

    }

    public HttpResponse postHttpResponse(WebServiceRequest webServiceRequest) {
        Logger.info("call " + webServiceRequest.callType + "'s get(" + webServiceRequest.url + ")...");
        WebServiceCallLogData log = createWebServiceCallLog(webServiceRequest.callType, "GET", webServiceRequest.url,
                webServiceRequest.keyword1,
                webServiceRequest.keyword2, webServiceRequest.keyword3);
        log.requestBody = webServiceRequest.requestBody;
        if (webServiceRequest.params != null) {
            log.postParams = new Gson().toJson(webServiceRequest.params);
        }

        long startTime = System.currentTimeMillis();
        try {
            HttpResponse response = doPost(webServiceRequest, log);
            long endTime = System.currentTimeMillis();
            log.duration = endTime - startTime; // 记录耗时
            log.responseText = response.getString();
            log.statusCode = response.getStatus();
            log.success = Boolean.TRUE;
            if (StringUtils.isNotBlank(webServiceRequest.callType)) {
                sendLogToMQ(log);
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
                sendLogToMQ(log);
            }

            throw e;
        }

    }

    protected WebServiceCallLogData createWebServiceCallLog(String callType, String callMethod, String url,
                                                         String keyword1, String keyword2, String keyword3) {
        WebServiceCallLogData log = new WebServiceCallLogData();

        log.callType = callType;
        log.callMethod = callMethod;
        log.key1 = keyword1;
        log.key2 = keyword2;
        log.key3 = keyword3;
        log.url = url;
        return log;
    }

    protected abstract HttpResponse doGet(WebServiceRequest webServiceRequest, WebServiceCallLogData log);

    protected abstract HttpResponse doPost( WebServiceRequest webServiceRequest, WebServiceCallLogData log);

    protected void sendLogToMQ(WebServiceCallLogData log) {
        MQPublisher.publish(MQ_KEY, log);
    }
}
