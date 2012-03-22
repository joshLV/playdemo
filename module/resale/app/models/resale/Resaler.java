package models.resale;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

import models.consumer.User;
import play.data.validation.Email;
import play.data.validation.Match;
import play.data.validation.MaxSize;
import play.data.validation.Min;
import play.data.validation.MinSize;
import play.data.validation.Phone;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
@Table(name="resaler")
public class Resaler extends Model {

	/**
	 * 分销商账户类型
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "account_type")
	public AccountType accountType;        

	@Column(name="login_name")
	@Required
	@MinSize(value = 2)
	@MaxSize(value = 6)
	public String loginName;

	@Column(name = "encrypted_password")
	@Required
	public String password;

	/**
	 * 分销商联系人姓名
	 */
	@Column(name="user_name")
	@Required
	@MaxSize(value = 255)
	public String userName;

	@Transient
	@Required
	public String confirmPassword;

	@Column(name="password_salt")
	public String passwordSalt;

	@Required
	@Match(value="^1[3|4|5|8][0-9]\\d{4,8}$",message="手机格式不匹配！")
	public String mobile;

	@Required
	@Phone
	public String phone;

	@Column(name="last_login_at")
	public Date lastLoginAt;

	@Column(name="email")
	@Required
	@Email
	public String email; 

	@Required
	public String address; 

	@Column(name="postcode")
	@Min(value=0)
	public String postCode; 

	@Column(name="identity_no")
	@Required
	public String identityNo; 

	/**
	 * 分销商状态
	 */
	@Enumerated(EnumType.STRING)
	public ResalerStatus status;

	/**
	 * 分销商等级
	 */
	@Enumerated(EnumType.STRING)
	public ResalerLevel level;

	@Column(name="login_ip")
	public String loginIp;

	@Column(name = "created_at")
	public Date createdAt;

	@Column(name = "updated_at")
	public Date updatedAt;
	
	/**
	 * 判断用户名和手机是否唯一
	 *
	 * @param loginName 用户名
	 * @param mobile 手机
	 */
	public static String checkValue(String loginName, String mobile) {

		List<Resaler> cuserList = Resaler.find("byLoginName", loginName).fetch();
		String returnFlag = "0";
		//用户名存在的情况
		if (cuserList.size() >0) returnFlag = "1";
		else {
			//手机存在的情况
			List<Resaler> mList = Resaler.find("byMobile", mobile).fetch();
			if(mList.size()>0) returnFlag = "2";
		}

		return returnFlag;
	}
}
