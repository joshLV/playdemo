package models.consumer;

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
@Table(name = "user_login_histories")
public class UserLoginHistory extends Model {
    
    private static final long serialVersionUID = 8123206013062L;
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public User user;

    @Column(name="application_name", length=50)
    public String applicationName;
    
    @Column(name="login_at")
    public Date loginAt;
    
    @Column(name="login_ip", length=50)
    public String loginIp;
    
    @Column(name="session_id", length=50)
    public String sessionId;
}
