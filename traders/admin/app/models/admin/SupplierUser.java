package models.admin;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name = "cuser")
public class SupplierUser extends Model {

    @Column(name = "login_name")
    public String loginName;

    public String mobile;

    @Column(name = "encrypted_password")
    public String encryptedPassword;

    @Column(name = "password_salt")
    public String passwordSalt;

    @Column(name = "last_login_ip")
    public String lastLoginIP;

    @Column(name = "last_login_at")
    public Date lastLoginAt;

    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;

}
