package models.journal;

import java.io.Serializable;
import java.util.Date;

/**
 * User: tanglq
 * Date: 13-1-17
 * Time: 下午8:18
 */
public class WebServiceCallLogData implements Serializable {

    private static final long serialVersionUID = 169932031130362L;

    public String callType;

    public String callMethod;

    public String url;

    /**
     * POST参数，JSON格式，形式为{key1=value1,key2=value2}
     */
    public String postParams;

    /**
     * 业务关键字1，用于辅助callType查询和分类。
     */
    public String key1;

    /**
     * 业务关键字2，用于辅助callType查询和分类。
     */
    public String key2;

    /**
     * 业务关键字3，用于辅助callType查询和分类。
     */
    public String key3;

    /**
     * HTTP返回码.
     */
    public Integer statusCode;

    /**
     * Request请求体.
     */
    public String requestBody;

    public String responseText;

    public String exceptionText;

    public Date createdAt = new Date();

    public Long duration;

    public Boolean success;

    public WebServiceCallLog toModel() {
        WebServiceCallLog log = new WebServiceCallLog();
        log.callType = this.callType;
        log.callMethod = this.callMethod;
        log.url = this.url;
        log.key1 = this.key1;
        log.key2 = this.key2;
        log.key3 = this.key3;
        log.postParams = this.postParams;
        log.requestBody = this.requestBody;
        log.responseText = this.responseText;
        log.exceptionText = this.exceptionText;
        log.createdAt = this.createdAt;
        log.duration = this.duration;
        log.success = this.success;

        return log;
    }
}
