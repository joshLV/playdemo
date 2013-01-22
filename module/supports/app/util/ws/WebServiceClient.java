package util.ws;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import models.journal.WebServiceCallLogData;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import play.Logger;
import play.libs.WS.HttpResponse;
import util.mq.MQPublisher;

import java.util.Map;

public abstract class WebServiceClient {

    public static final String MQ_KEY = "ws.call-log";

    public String encoding;

    public String getString(String callType, String url, String keyword, WebServiceCallback callback) {
        return getString(callType, url, keyword, null, null, callback);
    }

    public String getString(String callType, String url, String keyword1, String keyword2, WebServiceCallback callback) {
        return getString(callType, url, keyword1, keyword2, null, callback);
    }

    public String getString(String callType, String url, String keyword1, String keyword2, String keyword3, WebServiceCallback callback) {
        HttpResponse response = getHttpResponse(callType, url, keyword1, keyword2, keyword3, callback);
        return response.getString();
    }

    public String getString(String callType, String url, String keyword) {
        return getString(callType, url, keyword, null, null, null);
    }

    public String getString(String callType, String url, String keyword1, String keyword2) {
        return getString(callType, url, keyword1, keyword2, null, null);
    }

    public String getString(String callType, String url, String keyword1, String keyword2, String keyword3) {
        return getString(callType, url, keyword1, keyword2, keyword3, null);
    }

    public Document getXml(String callType, String url, String keyword, WebServiceCallback callback) {
        return getXml(callType, url, keyword, null, null, callback);
    }

    public Document getXml(String callType, String url, String keyword1, String keyword2, WebServiceCallback callback) {
        return getXml(callType, url, keyword1, keyword2, null, callback);
    }

    public Document getXml(String callType, String url, String keyword1, String keyword2, String keyword3, WebServiceCallback callback) {
        HttpResponse response = getHttpResponse(callType, url, keyword1, keyword2, keyword3, callback);
        if (this.encoding != null) {
            return response.getXml(this.encoding);
        }
        return response.getXml();
    }

    public Document getXml(String callType, String url, String keyword) {
        return getXml(callType, url, keyword, null, null, null);
    }

    public Document getXml(String callType, String url, String keyword1, String keyword2) {
        return getXml(callType, url, keyword1, keyword2, null, null);
    }

    public Document getXml(String callType, String url, String keyword1, String keyword2, String keyword3) {
        return getXml(callType, url, keyword1, keyword2, keyword3, null);
    }

    public JsonElement getJson(String callType, String url, String keyword, WebServiceCallback callback) {
        return getJson(callType, url, keyword, null, null, callback);
    }

    public JsonElement getJson(String callType, String url, String keyword1, String keyword2, WebServiceCallback callback) {
        return getJson(callType, url, keyword1, keyword2, null, callback);
    }

    public JsonElement getJson(String callType, String url, String keyword1, String keyword2, String keyword3, WebServiceCallback callback) {
        HttpResponse response = getHttpResponse(callType, url, keyword1, keyword2, keyword3, callback);
        return response.getJson();
    }

    public JsonElement getJson(String callType, String url, String keyword) {
        return getJson(callType, url, keyword, null, null, null);
    }

    public JsonElement getJson(String callType, String url, String keyword1, String keyword2) {
        return getJson(callType, url, keyword1, keyword2, null, null);
    }

    public JsonElement getJson(String callType, String url, String keyword1, String keyword2, String keyword3) {
        return getJson(callType, url, keyword1, keyword2, keyword3, null);
    }

    public String postString(String callType, String url, Map<String, Object> params, String keyword, WebServiceCallback callback) {
        return postString(callType, url, params, keyword, null, null, callback);
    }

    public String postString(String callType, String url, Map<String, Object> params, String keyword1, String keyword2, WebServiceCallback callback) {
        return postString(callType, url, params, keyword1, keyword2, null, callback);
    }

    public String postString(String callType, String url, Map<String, Object> params, String keyword1, String keyword2, String keyword3, WebServiceCallback callback) {
        HttpResponse response = postHttpResponse(callType, url, params, keyword1, keyword2, keyword3, callback);
        return response.getString();
    }

    public String postString(String callType, String url, Map<String, Object> params, String keyword) {
        return postString(callType, url, params, keyword, null, null, null);
    }

    public String postString(String callType, String url, Map<String, Object> params, String keyword1, String keyword2) {
        return postString(callType, url, params, keyword1, keyword2, null, null);
    }

    public String postString(String callType, String url, Map<String, Object> params, String keyword1, String keyword2, String keyword3) {
        return postString(callType, url, params, keyword1, keyword2, keyword3, null);
    }

    public Document postXml(String callType, String url, Map<String, Object> params, String keyword, WebServiceCallback callback) {
        return postXml(callType, url, params, keyword, null, null, callback);
    }

    public Document postXml(String callType, String url, Map<String, Object> params, String keyword1, String keyword2, WebServiceCallback callback) {
        return postXml(callType, url, params, keyword1, keyword2, null, callback);
    }

    public Document postXml(String callType, String url, Map<String, Object> params, String keyword1, String keyword2, String keyword3, WebServiceCallback callback) {
        HttpResponse response = postHttpResponse(callType, url, params, keyword1, keyword2, keyword3, callback);
        if (this.encoding != null) {
            return response.getXml(this.encoding);
        }
        return response.getXml();
    }

    public Document postXml(String callType, String url, Map<String, Object> params, String keyword) {
        return postXml(callType, url, params, keyword, null, null, null);
    }

    public Document postXml(String callType, String url, Map<String, Object> params, String keyword1, String keyword2) {
        return postXml(callType, url, params, keyword1, keyword2, null, null);
    }

    public Document postXml(String callType, String url, Map<String, Object> params, String keyword1, String keyword2, String keyword3) {
        return postXml(callType, url, params, keyword1, keyword2, keyword3, null);
    }

    public JsonElement postJson(String callType, String url, Map<String, Object> params, String keyword, WebServiceCallback callback) {
        return postJson(callType, url, params, keyword, null, null, callback);
    }

    public JsonElement postJson(String callType, String url, Map<String, Object> params, String keyword1, String keyword2, WebServiceCallback callback) {
        return postJson(callType, url, params, keyword1, keyword2, null, callback);
    }

    public JsonElement postJson(String callType, String url, Map<String, Object> params, String keyword1, String keyword2, String keyword3, WebServiceCallback callback) {
        HttpResponse response = postHttpResponse(callType, url, params, keyword1, keyword2, keyword3, callback);
        return response.getJson();
    }

    public JsonElement postJson(String callType, String url, Map<String, Object> params, String keyword) {
        return postJson(callType, url, params, keyword, null, null, null);
    }

    public JsonElement postJson(String callType, String url, Map<String, Object> params, String keyword1, String keyword2) {
        return postJson(callType, url, params, keyword1, keyword2, null, null);
    }

    public JsonElement postJson(String callType, String url, Map<String, Object> params, String keyword1, String keyword2, String keyword3) {
        return postJson(callType, url, params, keyword1, keyword2, keyword3, null);
    }

    // ---------- POST with body -----------
    public String postStringWithBody(String callType, String url, String body, String keyword, WebServiceCallback callback) {
        return postStringWithBody(callType, url, keyword, null, null, callback);
    }

    public String postStringWithBody(String callType, String url, String body, String keyword1, String keyword2, WebServiceCallback callback) {
        return postStringWithBody(callType, url, keyword1, keyword2, null, callback);
    }

    public String postStringWithBody(String callType, String url, String body, String keyword1, String keyword2, String keyword3, WebServiceCallback callback) {
        HttpResponse response = postHttpResponse(callType, url, body, null, keyword1, keyword2, keyword3, callback);
        return response.getString();
    }

    public String postStringWithBody(String callType, String url, String body, String keyword) {
        return postStringWithBody(callType, url, body, keyword, null, null, null);
    }

    public String postStringWithBody(String callType, String url, String body, String keyword1, String keyword2) {
        return postStringWithBody(callType, url, body, keyword1, keyword2, null, null);
    }

    public String postStringWithBody(String callType, String url, String body, String keyword1, String keyword2, String keyword3) {
        return postStringWithBody(callType, url, body, keyword1, keyword2, keyword3, null);
    }

    public Document postXmlWithBody(String callType, String url, String body, String keyword, WebServiceCallback callback) {
        return postXmlWithBody(callType, url, keyword, null, null, callback);
    }

    public Document postXmlWithBody(String callType, String url, String body, String keyword1, String keyword2, WebServiceCallback callback) {
        return postXmlWithBody(callType, url, keyword1, keyword2, null, callback);
    }

    public Document postXmlWithBody(String callType, String url, String body, String keyword1, String keyword2, String keyword3, WebServiceCallback callback) {
        HttpResponse response = postHttpResponse(callType, url, body, null, keyword1, keyword2, keyword3, callback);
        return response.getXml();
    }

    public Document postXmlWithBody(String callType, String url, String body, String keyword) {
        return postXmlWithBody(callType, url, body, keyword, null, null, null);
    }

    public Document postXmlWithBody(String callType, String url, String body, String keyword1, String keyword2) {
        return postXmlWithBody(callType, url, body, keyword1, keyword2, null, null);
    }

    public Document postXmlWithBody(String callType, String url, String body, String keyword1, String keyword2, String keyword3) {
        return postXmlWithBody(callType, url, body, keyword1, keyword2, keyword3, null);
    }

    public JsonElement postJsonWithBody(String callType, String url, String body, String keyword, WebServiceCallback callback) {
        return postJsonWithBody(callType, url, body, keyword, null, null, callback);
    }

    public JsonElement postJsonWithBody(String callType, String url, String body, String keyword1, String keyword2, WebServiceCallback callback) {
        return postJsonWithBody(callType, url, body, keyword1, keyword2, null, callback);
    }

    public JsonElement postJsonWithBody(String callType, String url, String body, String keyword1, String keyword2, String keyword3, WebServiceCallback callback) {
        HttpResponse response = postHttpResponse(callType, url, body, null, keyword1, keyword2, keyword3, callback);
        return response.getJson();
    }

    public JsonElement postJsonWithBody(String callType, String url, String body, String keyword) {
        return postJsonWithBody(callType, url, body, keyword, null, null, null);
    }

    public JsonElement postJsonWithBody(String callType, String url, String body, String keyword1, String keyword2) {
        return postJsonWithBody(callType, url, body, keyword1, keyword2, null, null);
    }

    public JsonElement postJsonWithBody(String callType, String url, String body, String keyword1, String keyword2, String keyword3) {
        return postJsonWithBody(callType, url, body, keyword1, keyword2, keyword3, null);
    }

    public HttpResponse getHttpResponse(String callType, String url, String keyword1, String keyword2, String keyword3, WebServiceCallback callback) {
        // 考虑到在MQ调用时没有打开数据库连接，这里重新开一下
        Logger.info("call " + callType + "'s get(" + url + ")...");
        WebServiceCallLogData log = createWebServiceCallLog(callType, "GET", url, keyword1, keyword2, keyword3);

        long startTime = System.currentTimeMillis();
        try {
            HttpResponse response = doGet(log, callback);
            long endTime = System.currentTimeMillis();
            log.duration = endTime - startTime; // 记录耗时
            log.responseText = response.getString();
            log.statusCode = response.getStatus();
            log.success = Boolean.TRUE;
            if (StringUtils.isNotBlank(callType)) {
                sendToMQ(log);
            }
            return response;
        } catch (Exception e) {
            Logger.error("getHttpResponse(callType:" + callType + ", url:" + url + "...) exception:" +
                    e.getMessage(), e);
            if (StringUtils.isNotBlank(callType)) {
                long endTime = System.currentTimeMillis();
                log.duration = endTime - startTime; // 记录耗时
                log.exceptionText = e.getMessage();
                log.statusCode = -1;
                log.success = Boolean.FALSE;
                sendToMQ(log);
            }
            throw e;
        }

    }

    public HttpResponse postHttpResponse(String callType, String url, Map<String, Object> params, String keyword1, String keyword2, String keyword3, WebServiceCallback callback) {
        return postHttpResponse(callType, url, null, params, keyword1, keyword2, keyword3, callback);
    }

    public HttpResponse postHttpResponse(String callType, String url, String body, Map<String, Object> params, String keyword1, String keyword2, String keyword3, WebServiceCallback callback) {
        Logger.info("call " + callType + "'s get(" + url + ")...");
        WebServiceCallLogData log = createWebServiceCallLog(callType, "GET", url, keyword1, keyword2, keyword3);
        log.requestBody = body;
        if (params != null) {
            log.postParams = new Gson().toJson(params);
        }

        long startTime = System.currentTimeMillis();
        try {
            HttpResponse response = doPost(log, params, callback);
            long endTime = System.currentTimeMillis();
            log.duration = endTime - startTime; // 记录耗时
            log.responseText = response.getString();
            log.statusCode = response.getStatus();
            log.success = Boolean.TRUE;
            if (StringUtils.isNotBlank(callType)) {
                sendToMQ(log);
            }
            return response;
        } catch (Exception e) {
            Logger.error("postHttpResponse(callType:" + callType + ", url:" + url + "...) exception:" +
                    e.getMessage(), e);
            if (StringUtils.isNotBlank(callType)) {
                long endTime = System.currentTimeMillis();
                log.duration = endTime - startTime; // 记录耗时
                log.exceptionText = e.getMessage();
                log.statusCode = -1;
                log.success = Boolean.FALSE;
                sendToMQ(log);
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

    protected abstract HttpResponse doGet(WebServiceCallLogData log, WebServiceCallback callback);

    protected abstract HttpResponse doPost(WebServiceCallLogData log, Map<String, Object> params, WebServiceCallback callback);

    public void sendToMQ(WebServiceCallLogData log) {
        MQPublisher.publish(MQ_KEY, log);
    }
}
