package controllers;

import models.consumer.User;
import models.consumer.UserInfo;
import models.sms.SMSUtil;

import org.apache.commons.lang.StringUtils;

import play.cache.Cache;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import com.uhuila.common.util.RadomNumberUtil;

import controllers.modules.website.cas.SecureCAS;

@With(SecureCAS.class)
public class UserInfos extends Controller{

	/**
	 * 用户资料页面 
	 */
	public static void index() {
		User user = SecureCAS.getUser();
		UserInfo userInfo = UserInfo.findByUser(user);
		render(user,userInfo);
	}

	/**
	 * 用户资料页面 
	 */
	public static void update(Long id,UserInfo userInfo,String intrest) {
		UserInfo userInfos = UserInfo.findById(id);
		//如果用户信息不存在则创建
		if (userInfos != null) {
			//存在则修改
			userInfos.update(userInfo,intrest);
			//同时更新用户手机
			User user = SecureCAS.getUser();
			user.updateMobile(userInfo.mobile);
		}
		index();
	}

	/**
	 * 发送验证码
	 * 
	 * @param mobile 手机
	 */
	public static void sendValidCode(String mobile) {

		String validCode = RadomNumberUtil.generateSerialNumber(4);
		String comment="您的验证码是"+validCode+", 请将该号码输入后即可验证成功。如非本人操作，请及时修改密码";
		SMSUtil.send(comment, mobile);
		//保存手机和验证码
		Cache.set("validCode_", validCode, "10mn");
		Cache.set("mobile_", mobile, "10mn");
		renderJSON("1");
	}

	/**
	 * 绑定手机
	 * 
	 * @param mobile 手机
	 */
	public static void bindMobile(String mobile,String validCode) {
		Object objCode = Cache.get("validCode_");
		Object objMobile = Cache.get("mobile_");
		String cacheValidCode = objCode== null ?"":objCode.toString();
		String cacheMobile =  objMobile== null ?"":objMobile.toString();
		//判断验证码是否正确
		if(!StringUtils.normalizeSpace(cacheValidCode).equals(validCode)) {
			renderJSON("1");
		}
		//判断手机是否正确
		if(!StringUtils.normalizeSpace(cacheMobile).equals(cacheMobile)) {
			renderJSON("2");
		}
		//更新用户基本信息手机
		User user = SecureCAS.getUser();
		UserInfo.updateById(user,mobile);
		//同时更新用户手机
		user.updateMobile(mobile);

		Cache.delete("validCode_");
		Cache.delete("mobile_");
		renderJSON("0");
	}

}
