package models.sms;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

/**
 * 虚拟短信，把短信内容保存到数据库。
 * @author tanglq
 *
 */
@Entity
@Table(name="vx_sms")
public class VxSms extends Model {

    @Column(length=100)
    public String mobile;
    
    @Column(length=2000)
    public String message;
    
    @Column(name="created_at")
    public Date createdAt;
    
    @Column(name="sys_type")
    public String smsType;
    
}
