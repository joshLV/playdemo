package controllers;

import models.consumer.User;
import models.consumer.UserInfo;
import models.sms.SMSUtil;

import org.apache.commons.lang.StringUtils;

import play.cache.Cache;
import play.data.validation.Validation;
import play.libs.Codec;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.website.cas.SecureCAS;

@With(SecureCAS.class)
public class UserInfos extends Controller{

	/**
	 * 用户资料页面 
	 */
	public static void index() {
		User user = SecureCAS.getUser();
		UserInfo userInfo = UserInfo.findById(user.id);
		String birth = userInfo.birthday;
		if (StringUtils.isNotBlank(birth)) {
			userInfo.birthdayYear=birth.substring(0,4);
			userInfo.birthdayMonth=birth.substring(4,6);
			userInfo.birthdayDay=birth.substring(6,8);
		}

		render(user,userInfo);
	}

	/**
	 * 用户资料页面 
	 */
	public static void update(UserInfo userInfo) {

		if (Validation.hasErrors()) {
			render("UserInfos/index.html", userInfo);
		}
		User user = SecureCAS.getUser();
		UserInfo userInfos = UserInfo.findById(user.id);
		//如果用户信息不存在则创建
		if (userInfos == null) {
			userInfo.save();
		} else {
			//存在则修改
			userInfos.update(userInfo);
		}
		String isOk="ok";
		render("UserInfos/index.html",user,userInfo,isOk);
	}

	/**
	 * 发送验证码
	 * 
	 * @param mobile 手机
	 */
	public static void sendValidCode(String mobile) {
//		String validCode = RadomNumberUtil.generateSerialNumber(4);
//		String comment="您的验证码是"+validCode+", 请将该号码输入后即可验证成功。如非本人操作，请及时修改密码";
//		SMSUtil.send(comment, mobile);
//		//保存手机和验证码
//		Cache.set("validCode_"+validCode, validCode.toUpperCase(), "10mn");
//		Cache.set("mobile_"+mobile, validCode.toUpperCase(), "10mn");
//		renderJSON("1");
	}

	/**
	 * 绑定手机
	 * 
	 * @param mobile 手机
	 */
	public static void bindMobile(String mobile,String validCode) {
		String cacheValidCode = Cache.get("validCode_" + validCode).toString();
		String cacheMobile = Cache.get("mobile_" + mobile).toString();
		//判断验证码是否正确
		if(!StringUtils.normalizeSpace(cacheValidCode).equals(validCode)) {
			renderJSON("1");
		}
		//判断手机是否正确
		if(!StringUtils.normalizeSpace(cacheMobile).equals(cacheMobile)) {
			renderJSON("2");
		}
		User user = SecureCAS.getUser();
		UserInfo.updateById(user.id,mobile);
		Cache.delete("validCode_"+validCode);
		Cache.delete("mobile_"+mobile);
		renderJSON("0");
	}

}
