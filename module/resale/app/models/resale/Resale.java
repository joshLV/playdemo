package models.resale;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

import models.accounts.AccountType;
import play.db.jpa.Model;

@Entity
@Table(name="resaler")
public class Resale extends Model {

	/**
	 * 分销商账户类型
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "account_type")
	public AccountType accountType;        

	@Column(name="lonin_name")
	public String loginName;

	@Column(name = "encrypted_password")
	public String password;

	/**
	 * 分销商联系人姓名
	 */
	@Column(name="user_name")
	public String userName;

	@Transient
	public String confirmPassword;

	@Column(name="password_salt")
	public String passwordSalt;

	public String mobile;

	public String phone;

	@Column(name="last_login_at")
	public Date lastLoginAt;

	@Column(name="email")
	public String email; 

	public String address; 

	@Column(name="postcode")
	public String postCode; 

	@Column(name="identity_no")
	public String identityNo; 

	/**
	 * 分销商状态
	 */
	@Enumerated(EnumType.STRING)
	public ResaleStatus status;

	/**
	 * 分销商等级
	 */
	@Enumerated(EnumType.STRING)
	public ResaleLevel level;

	@Column(name="login_ip")
	public String loginIp;

	@Column(name = "created_at")
	public Date createdAt;

	@Column(name = "updated_at")
	public Date updatedAt;
}
