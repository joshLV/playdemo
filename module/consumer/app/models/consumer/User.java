package models.consumer;

import java.util.*;
import javax.persistence.*;

import play.db.jpa.*;

@Entity
@Table(name = "users")
public class User extends Model {

    @Column(name="login_name")
    public String loginName;

    public String email;

    public String mobile;

    @Column(name="openid_souce")
    public String openIdSource;

    public String password;

    @Column(name="pwd_salt")
    public String passwordSalt;

    @Column(name="last_login_at")
    public Date lastLoginAt;

    public int status;

    @Column(name="login_ip")
    public String loginIp;
}
