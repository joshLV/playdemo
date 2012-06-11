package models.admin;

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
@Table(name="supplier_user_login_histories")
public class SupplierUserLoginHistory extends Model {
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_user_id")
    public SupplierUser user;

    @Column(name="application_name")
    public String applicationName;
    
    @Column(name="login_at")
    public Date loginAt;
    
    @Column(name="login_ip")
    public String loginIp;
    
    @Column(name="session_id", length=50)
    public String sessionId;
}
