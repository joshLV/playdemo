package util.ws;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import models.journal.WebServiceCallLog;
import models.journal.WebServiceCallType;

import org.w3c.dom.Document;

import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.libs.WS.HttpResponse;

import com.google.gson.JsonElement;

public abstract class WebServiceClientHelper {

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

    public HttpResponse getHttpResponse(String callType, String url, String keyword1, String keyword2, String keyword3, WebServiceCallback callback) {
        // 考虑到在MQ调用时没有打开数据库连接，这里重新开一下
        boolean jpaBeenDisabled = false;
        if (!JPA.isEnabled()) {
            JPAPlugin.startTx(false);
            jpaBeenDisabled = true;
        }
        Logger.info("call " + callType + "'s get(" + url + ")...");
        WebServiceCallLog log = createWebServiceCallLog(callType, "GET", url,
                        keyword1, keyword2, keyword3);
        
        try {
            long startTime = System.currentTimeMillis();
            HttpResponse response = doGet(log, callback);
            long endTime = System.currentTimeMillis();
            log.duration = endTime - startTime; // 记录耗时
            log.responseText = response.getString();
            log.statusCode = response.getStatus();
            log.success = Boolean.TRUE;
            log.save();
            return response;
        } catch (Exception e) {
            log.success = Boolean.FALSE;
            StringWriter stringWriter = new StringWriter();
            PrintWriter pw = new PrintWriter(stringWriter);
            e.printStackTrace(pw);
            log.exceptionText = stringWriter.toString();
            log.save();
            throw e;
        } finally {
            if (jpaBeenDisabled) {
                // FIXME: 如何保证在其它事务先打开的情况下，也记录日志？考虑把记录日志的功能做成内部web服务，这样总是会保存
                JPAPlugin.closeTx(false);
            }
        }
        
    }

    public HttpResponse postHttpResponse(String callType, String url, Map<String, Object> params,
                    String keyword1, String keyword2, String keyword3, WebServiceCallback callback) {
        // 考虑到在MQ调用时没有打开数据库连接，这里重新开一下
        boolean jpaBeenDisabled = false;
        if (!JPA.isEnabled()) {
            JPAPlugin.startTx(false);
            jpaBeenDisabled = true;
        }
        Logger.info("call " + callType + "'s get(" + url + ")...");
        WebServiceCallLog log = createWebServiceCallLog(callType, "GET", url,
                        keyword1, keyword2, keyword3);
        
        try {
            long startTime = System.currentTimeMillis();
            HttpResponse response = doPost(log, params, callback);
            long endTime = System.currentTimeMillis();
            log.duration = endTime - startTime; // 记录耗时
            log.responseText = response.getString();
            log.statusCode = response.getStatus();
            log.success = Boolean.TRUE;
            log.save();
            return response;
        } catch (Exception e) {
            log.success = Boolean.FALSE;
            StringWriter stringWriter = new StringWriter();
            PrintWriter pw = new PrintWriter(stringWriter);
            e.printStackTrace(pw);
            log.responseText = stringWriter.toString();
            log.save();
            throw e;
        } finally {
            if (jpaBeenDisabled) {
                // FIXME: 如何保证在其它事务先打开的情况下，也记录日志？考虑把记录日志的功能做成内部web服务，这样总是会保存
                JPAPlugin.closeTx(false);
            }
        }
        
    }
    
    protected WebServiceCallLog createWebServiceCallLog(String callType,
                    String callMethod, String url, String keyword1, String keyword2,
                    String keyword3) {
        
        WebServiceCallType.checkOrCreate(callType);
        
        WebServiceCallLog log = new WebServiceCallLog();
        
        log.callType = callType;
        log.callMethod = callMethod;
        log.key1 = keyword1;
        log.key2 = keyword2;
        log.key3 = keyword3;
        log.url = url;
        return log;
    }

    protected abstract HttpResponse doGet(WebServiceCallLog log, WebServiceCallback callback);

    protected abstract HttpResponse doPost(WebServiceCallLog log,
                    Map<String, Object> params, WebServiceCallback callback);

}
