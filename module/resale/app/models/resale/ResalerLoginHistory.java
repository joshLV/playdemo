package models.resale;

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
@Table(name="resaler_login_histories")
public class ResalerLoginHistory extends Model {
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "resaler_id")
    public Resaler resaler;

    @Column(name="application_name", length=50)
    public String applicationName;
    
    @Column(name="login_at")
    public Date loginAt;
    
    @Column(name="login_ip", length=50)
    public String loginIp;
    
    @Column(name="session_id", length=50)
    public String sessionId;
}
