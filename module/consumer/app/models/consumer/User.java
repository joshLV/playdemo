package models.consumer;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import models.resale.Resaler;

import org.apache.commons.codec.digest.DigestUtils;

import play.data.validation.Email;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.Images;
import play.modules.view_ext.annotation.Mobile;

@Entity
@Table(name = "users")
public class User extends Model {

	@Column(name="email")
	@Required
	@Email
	public String loginName;

	@Mobile
	@Required
	public String mobile;

	@Column(name="openid_source")
	public String openIdSource;

	@Column(name = "encrypted_password")
	@Required
	@MinSize(value = 6)
	@MaxSize(value = 20)
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

	@Transient
	public String oldPassword;


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

	/**
	 * 修改密码
	 * 
	 * @param newUser 新密码信息
	 * @param user 原密码信息
	 */
	public static void updatePassword(User newUser, User user) {
		// 随机码
		Images.Captcha captcha = Images.captcha();
		String newPasswordSalt = captcha.getText(6);
		newUser.passwordSalt = newPasswordSalt;
		// 新密码
		String newPassword = user.password;
		newUser.password = DigestUtils.md5Hex(newPassword + newPasswordSalt);
		newUser.save();

	}
}
