package models.consumer;

import play.data.validation.Email;
import play.data.validation.Match;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends Model {

	@Column(name="email")
	@Required
	@Email
	public String loginName;

	@Match(value="^1[3|4|5|8][0-9]\\d{4,8}$",message="手机格式不匹配！")
	@Required
	public String mobile;

	@Column(name="openid_source")
	public String openIdSource;

	@Column(name = "encrypted_password")
	@Required
	public String password;

	@Transient
	@Required
	public String confirmPassword;
	@Transient
	@Required
	public String captcha;

	@Column(name="password_salt")
	public String passwordSalt;

	@Column(name="last_login_at")
	public Date lastLoginAt;

	public int status;

	@Column(name="login_ip")
	public String loginIp;

	@Column(name="created_at")
	public Date createdAt;
	
	/**
	 * 判断用户名和手机是否唯一
	 *
	 * @param loginName 用户名
	 * @param mobile 手机
	 */
	public static String checkValue(String loginName, String mobile) {

		List<User> supplierUserList = User.find("byLoginName", loginName).fetch();
		String returnFlag = "0";
		//用户名存在的情况
		if (supplierUserList.size() >0) returnFlag = "1";
		else {
			//手机存在的情况
			List<User> mList = User.find("byMobile", mobile).fetch();
			if(mList.size()>0) returnFlag = "2";
		}

		return returnFlag;
	}
}
