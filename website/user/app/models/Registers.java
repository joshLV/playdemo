package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import play.data.validation.Email;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
@Table(name="users")
public class Registers extends Model{
	/** 邮箱*/
	public String email;
	/** 手机*/
	public String mobile;
	/**密码*/
	public String crypted_password;
	/**状态 0：正常 1：冻结*/
	public String status;
	/** 注册IP*/
	public String login_ip;
	/** 验证码*/
	public String password_salt;
}
