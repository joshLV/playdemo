package models.sms;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * @author likang
 *         Date: 12-8-16
 */
@Entity
@Table(name = "bind_mobiles")
public class BindMobile extends Model {
    @Column(name = "mobile")
    public String mobile;           //手机号

    @Enumerated(EnumType.STRING)
    @Column(name = "bind_type")
    public MobileBindType bindType; //绑定业务类型


    @Column(name = "bind_info")
    public String bindInfo;         //记录额外信息

    @Column(name = "bind_at")
    public Date bindAt;             //绑定时间

    public BindMobile(String mobile, MobileBindType bindType){
        this.mobile = mobile;
        this.bindType = bindType;

        this.bindInfo = null;
        this.bindAt = new Date();
    }
}
