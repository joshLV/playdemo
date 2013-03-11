package models.journal;

import com.uhuila.common.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 记录基于Http的服务调用日志.
 *
 * @author tanglq
 */
@Entity
@Table(name = "ws_call_logs")
public class WebServiceCallLog extends Model {

    private static final long serialVersionUID = 16993203113062L;

    @Column(name = "call_type", length = 250)
    public String callType;

    @Column(name = "call_method", length = 30)
    public String callMethod;

    @Column(length = 500)
    public String url;

    /**
     * POST参数，JSON格式，形式为{key1=value1,key2=value2}
     */
    @Column(name = "post_params", length = 2000)
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
    @Column(name = "status_code")
    public Integer statusCode;

    /**
     * Request请求体.
     */
    @Lob
    @Column(name = "request_body")
    public String requestBody;

    @Lob
    @Column(name = "response_text")
    public String responseText;

    @Lob
    @Column(name = "exception_text")
    public String exceptionText;

    @Column(name = "created_at")
    public Date createdAt = new Date();

    @Column(length = 1024)
    public String files;

    public Long duration;

    /**
     * 查询条件：开始时间.
     */
    @Transient
    public Date createdAtBegin = DateUtil.getBeginOfDay();

    /**
     * 查询条件：结束时间
     */
    @Transient
    public Date createdAtEnd = DateUtil.getEndOfDay(new Date());

    /**
     * 标识此记录是否是成功记录，如果不成功，则调用者有义务重新发起调用。
     * 一般MQ调用时，通过抛出异常来触发重试，这里需要在抛出异常前设置成功标记为False.
     */
    public Boolean success;

    public static JPAExtPaginator<WebServiceCallLog> query(
            WebServiceCallLog log,
            int pageNumber, int pageSize) {
        StringBuffer sql = new StringBuffer("1=1");
        Map params = new HashMap();

        if (StringUtils.isNotBlank(log.callType)) {
            sql.append(" and l.callType like :callType");
            params.put("callType", log.callType + "%");
        }
        if (StringUtils.isNotBlank(log.key1)) {
            sql.append(" and (key1 like :keyword or key2 like :keyword or key3 like :keyword)");
            params.put("keyword", log.key1 + "%");
        }
        if (log.createdAtBegin != null) {
            sql.append(" and l.createdAt >= :createdAtBegin");
            params.put("createdAtBegin", log.createdAtBegin);
        }
        if (log.createdAtEnd != null) {
            sql.append(" and l.createdAt < :createdAtEnd");
            params.put("createdAtEnd", DateUtil.getEndOfDay(log.createdAtEnd));
        }

        JPAExtPaginator<WebServiceCallLog> logPages = new JPAExtPaginator<>(
                "WebServiceCallLog l", "l",
                WebServiceCallLog.class, sql.toString(), params)
                .orderBy("l.createdAt desc");
        logPages.setPageNumber(pageNumber);
        logPages.setPageSize(pageSize);
        logPages.setBoundaryControlsEnabled(true);
        return logPages;
    }

    @Transient
    private WebServiceCallType webServiceCallType;

    @Transient
    public WebServiceCallType getWebServiceCallType() {
        if (webServiceCallType == null) {
            webServiceCallType = WebServiceCallType
                    .find("callType=?", callType).first();
        }
        return webServiceCallType;
    }

    @Transient
    public String getCallTypeName() {
        if (getWebServiceCallType() == null
                || StringUtils.isBlank(getWebServiceCallType().description)) {
            return callType;
        }
        return getWebServiceCallType().description;
    }

    @Transient
    public String getKey1Name() {
        if (getWebServiceCallType() == null
                || StringUtils.isBlank(getWebServiceCallType().key1Name)) {
            return key1;
        }
        return getWebServiceCallType().key1Name + ":" + key1;
    }

    @Transient
    public String getKey2Name() {
        if (getWebServiceCallType() == null
                || StringUtils.isBlank(getWebServiceCallType().key2Name)) {
            return key2;
        }
        return getWebServiceCallType().key2Name + ":" + key2;
    }

    @Transient
    public String getKey3Name() {
        if (getWebServiceCallType() == null
                || StringUtils.isBlank(getWebServiceCallType().key3Name)) {
            return key3;
        }
        return getWebServiceCallType().key3Name + ":" + key3;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("callType", callType).
                append("callMethod", callMethod).
                append("url", url).
                append("postParams", postParams).
                append("key1", key1).
                append("key2", key2).
                append("key3", key3).
                append("statusCode", statusCode).
                append("requestBody", requestBody).
                append("responseText", responseText).
                append("exceptionText", exceptionText).
                append("createdAt", createdAt).
                append("files", files).
                append("duration", duration).
                append("createdAtBegin", createdAtBegin).
                append("createdAtEnd", createdAtEnd).
                append("success", success).
                append("webServiceCallType", webServiceCallType).
                toString();
    }
}
