package models.journal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

/**
 * 用于自动记录WebServiceCallLog中的callType，方便查询。
 * @author tanglq
 */
@Entity
@Table(name="ws_call_type")
public class WebServiceCallType extends Model {
    @Column(name="call_type")
    public String callType;
    
    /**
     * 描述文字，如果有则可方便查询时得到提示.
     */
    public String description;
    
    @Column(name="key1_name")
    public String key1Name;
    
    @Column(name="key2_name")
    public String key2Name;
    
    @Column(name="key3_name")
    public String key3Name;
}
