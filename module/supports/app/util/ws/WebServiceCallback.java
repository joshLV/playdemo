package util.ws;

import models.journal.WebServiceCallLog;

/**
 * WebService客户端回调接口.
 *
 * 开发人员可通过在process方法中加入业务数据记录.
 * @author tanglq
 */
public abstract class WebServiceCallback {

    protected WebServiceCallLog log;

    protected void setWebServiceCallLog(WebServiceCallLog _log) {
        this.log = _log;
    }

    protected void setSuccessful(Boolean _success) {
        this.log.success = _success;
    }

    public abstract void process(int statusCode, String returnContent);
}
