package controllers;

import models.consumer.User;
import models.consumer.UserInfo;

import org.apache.commons.lang.StringUtils;

import play.cache.Cache;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.libs.Codec;
import play.libs.Images;
import play.mvc.Controller;

/**
 * 前台注册用户
 * 
 * @author yanjy
 *
 */
public class Register extends Controller{

	public static final String SESSION_USER_KEY = "website_login";

	/**
	 * 注册页面 
	 */
	public static void index() {
		render();
	}

	/**
	 *  注册新用户
	 * 
	 * @param user 用户信息
	 */
	public static void create(@Valid User user) {

		if (Validation.hasError("user.mobile")
				&& Validation.hasError("user")) {
			Validation.clear();
		}

		if(!user.password.equals(user.confirmPassword)){
			Validation.addError("user.confirmPassword", "validation.confirmPassword");
		}
		if(!"dev".equals(play.Play.configuration.get("application.mode"))) {
			if(StringUtils.isNotEmpty(user.captcha) && !user.captcha.toUpperCase().equals(Cache.get(params.get("randomID")))){
				Validation.addError("user.captcha", "validation.captcha");
			}
		}

		if (Validation.hasErrors()) {
			render("Register/index.html", user);
		}

		//用户创建
		user.create();
		user.userInfo = new UserInfo(user);
		user.save();

		session.put(SESSION_USER_KEY,user.loginName);
		render("Register/success.html");
	}


	/**
	 * 判断用户名和手机是否唯一
	 * 
	 * @param loginName
	 *            用户名
	 * @param mobile
	 *            手机
	 */
	public static void checkLoginName(String loginName, String mobile) {
		String returnFlag = User.checkLoginName(loginName);
		renderJSON(returnFlag);
	}

	/**
	 * 验证码
	 * @param id
	 */
	public static void captcha(String randomID) {
		Images.Captcha captcha = Images.captcha();

		String code = captcha.getText("#49B07F",4);
		Cache.set(randomID, code.toUpperCase(), "30mn");
		renderBinary(captcha);
	}

	/**
	 * UUID
	 * 
	 * @return
	 */
	public static String genRandomId(){
		return Codec.UUID();
	}

}
