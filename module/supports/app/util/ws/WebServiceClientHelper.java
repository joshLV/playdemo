package util.ws;

import models.journal.WebServiceCallLog;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;

public abstract class WebServiceClientHelper {

    public void get(String callType, String url, String keyword, WebServiceCallback callback) {
        get(callType, url, keyword, null, null, callback);
    }

    public void get(String callType, String url, String keyword1, String keyword2, WebServiceCallback callback) {
        get(callType, url, keyword1, keyword2, null, callback);
    }
    
    public void get(String callType, String url, String keyword1, String keyword2, String keyword3, WebServiceCallback callback) {
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
            doGet(log, callback);
            log.save();
        } catch (Exception e) {
            log.success = Boolean.FALSE;
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
        WebServiceCallLog log = new WebServiceCallLog();
        
        log.callType = callType;
        log.callMethod = callMethod;
        log.key1 = keyword1;
        log.key2 = keyword2;
        log.key3 = keyword3;
        log.url = url;
        return log;
    }

    protected abstract void doGet(WebServiceCallLog log, WebServiceCallback callback);

}
