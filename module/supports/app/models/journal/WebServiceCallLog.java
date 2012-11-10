package models.journal;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import play.db.jpa.Model;

/**
 * 记录基于Http的服务调用日志.
 * 
 * @author tanglq
 */
@Entity
@Table(name="ws_call_logs")
public class WebServiceCallLog extends Model {

    @Column(name="call_type", length=30)
    public String callType;
    
    @Column(name="call_method", length=10)
    public String callMethod;
    
    @Column(length=500)
    public String url;
    
    /**
     * POST参数，JSON格式，形式为{key1=value1,key2=value2}
     */
    @Column(name="post_params", length=1000)
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
    @Column(name="status_code")
    public Integer statusCode;
    
    @Lob
    @Column(name="response_text", length=4000)
    public String responseText;
    
    @Column(name="created_at")
    public Date createdAt = new Date();

    /**
     * 标识此记录是否是成功记录，如果不成功，则调用者有义务重新发起调用。
     * 一般MQ调用时，通过抛出异常来触发重试，这里需要在抛出异常前设置成功标记为False.
     */
    public Boolean success;
    
}
