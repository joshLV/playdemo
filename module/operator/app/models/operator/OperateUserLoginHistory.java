package models.operator;

import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import play.db.jpa.Model;

@Entity
@Table(name="operate_user_login_histories")
public class OperateUserLoginHistory extends Model {
    
    private static final long serialVersionUID = 2406119113062L;
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "operate_user_id")
    public OperateUser user;

    @Column(name="application_name")
    public String applicationName;
    
    @Column(name="login_at")
    public Date loginAt;
    
    @Column(name="login_ip")
    public String loginIp;
    
    @Column(name="session_id", length=50)
    public String sessionId;
}
