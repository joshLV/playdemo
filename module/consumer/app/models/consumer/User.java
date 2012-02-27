package models.consumer;

import java.util.*;
import javax.persistence.*;

import play.db.jpa.*;

@Entity
@Table(name = "users")
public class User extends Model {

    @Column(name="email")
    public String loginName;

    public String mobile;

    @Column(name="openid_source")
    public String openIdSource;

    @Column(name="password_salt")
    public String passwordSalt;

    @Column(name="last_login_at")
    public Date lastLoginAt;

    public int status;

    @Column(name="login_ip")
    public String loginIp;
}
