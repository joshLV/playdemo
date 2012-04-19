package models.consumer;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.view_ext.annotation.Mobile;

@Entity
@Table(name = "users_info")
public class UserInfo extends Model {

	@Column(name="user_name")
	public String userName;

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
	public String[] intrest;
	/*行业*/
	public String industry;


	@Column(name="created_at")
	public Date createdAt;


	/**
	 * 更新用户信息
	 * 
	 * @param userInfo 用户信息
	 */
	public void update(UserInfo userInfo) {
		userName=userInfo.userName;
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
		//userInfos.intrest=userInfo.intrest;
		marrState=userInfo.marrState;
		createdAt=new Date();
		save();

	}

	/**
	 * 更新手机
	 * 
	 * @param id 用户ID
	 * @param mobile 手机
	 */
	public static void updateById(Long id, String mobile) {
		UserInfo userInfos = UserInfo.findById(id);
		userInfos.mobile=mobile;
		userInfos.save();
	}

}
