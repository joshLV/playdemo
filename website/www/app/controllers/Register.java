package controllers;

import models.consumer.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.libs.Codec;
import play.libs.Images;
import play.mvc.Controller;

import java.util.Date;

/**
 * 前台注册用户
 * 
 * @author yanjy
 *
 */
public class Register extends Controller{

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
		if(!user.password.equals(user.confirmPassword)){
			Validation.addError("user.confirmPassword", "两次密码输入的不一样！！");
		}
		if(!"dev".equals(play.Play.configuration.get("application.mode"))) {
			if(StringUtils.isNotEmpty(user.captcha) && !user.captcha.toUpperCase().equals(Cache.get(params.get("randomID")))){
				Validation.addError("user.captcha", "验证码不对，请重新输入！");
			}
		}
		if (Validation.hasErrors()) {
			render("Register/index.html", user);
		}

		Images.Captcha captcha = Images.captcha();
		String passwordSalt=captcha.getText(6);
		//密码加密
		user.password=DigestUtils.md5Hex(user.password+passwordSalt);
		//正常
		user.status=1;
		//随机吗
		user.passwordSalt=passwordSalt;
		//获得IP
		user.loginIp=request.current().remoteAddress;
		user.lastLoginAt = new Date();
		user.createdAt = new Date();
		user.save();

		redirect("http://www.uhuila.cn");
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
		String returnFlag = User.checkValue(loginName, mobile);
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
