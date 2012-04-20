package models.consumer;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import play.db.jpa.Model;
import play.modules.view_ext.annotation.Mobile;

@Entity
@Table(name = "users_info")
public class UserInfo extends Model {
	@OneToOne(cascade = CascadeType.ALL)  
	@JoinColumn(name = "user_id")  
	public User user;

	@Column(name="full_name")
	public String fullName;

	@Mobile
	public String mobile;

	/**性别*/
	@Column(name="user_sex")
	public int userSex;

	/*出生年月*/
	public String birthday;
	/*年*/
	@Transient
	public String birthdayYear;
	/*月*/
	@Transient
	public String birthdayMonth;
	/*日*/
	@Transient
	public String birthdayDay;
	/*电话*/
	public String phone;

	/*婚姻状况*/
	public int marrState;
	/*职位*/
	public String position;
	/*qq*/
	public String userqq;
	/*薪水*/
	public int salary;
	/*职位*/
	public String intrest;
	/*行业*/
	public String industry;
	/*行业*/
	public String otherInfo;
	@Column(name="created_at")
	public Date createdAt;

	@Column(name="bindMobile_at")
	public Date bindMobileAt;

	public UserInfo(User user) {
		this.user =user;
		this.createdAt = new Date();
	}

	public UserInfo() {
	}

	/**
	 * 更新用户信息
	 * 
	 * @param userInfo 用户信息
	 */
	public void update(UserInfo userInfo,String intrests) {
		fullName=userInfo.fullName;
		salary=userInfo.salary;
		position=userInfo.position;
		phone=userInfo.phone;
		userqq=userInfo.userqq;
		userSex=userInfo.userSex;
		industry=userInfo.industry;
		String birthdayYear = userInfo.birthdayYear;
		if (StringUtils.isNotBlank(birthdayYear)) {
			birthday=userInfo.birthdayYear+userInfo.birthdayMonth+userInfo.birthdayDay;
		}
		intrest=intrests;
		marrState=userInfo.marrState;
		otherInfo = userInfo.otherInfo;
		createdAt=new Date();
		this.save();

	}

	/**
	 * 更新手机
	 * 
	 * @param id 用户ID
	 * @param mobile 手机
	 */
	public static void updateById(User user, String mobile) {
		List<UserInfo> userInfos = UserInfo.find("byUser",user).fetch();
		UserInfo userInfo = null;
		if (userInfos.size()>0) {
			userInfo = userInfos.get(0);
			userInfo.mobile=mobile;
			userInfo.bindMobileAt=new Date();
			userInfo.save();
		}
	}

	/**
	 * 查询用户基本信息
	 * 
	 * @param user 用户ID信息
	 * @return 用户基本信息
	 */
	public static UserInfo findByUser(User user) {
		List<UserInfo> userInfos = UserInfo.find("byUser",user).fetch();
		UserInfo userInfo = null;
		if (userInfos.size()>0) {
			userInfo = userInfos.get(0);
			String birth = userInfo.birthday;
			if (StringUtils.isNotBlank(birth)) {
				userInfo.birthdayYear=birth.substring(0,4);
				userInfo.birthdayMonth=birth.substring(4,6);
				userInfo.birthdayDay=birth.substring(6,8);
			}
		}

		return userInfo;
	}

}
