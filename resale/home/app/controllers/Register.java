package controllers;

import java.util.Date;

import models.resale.Resale;
import models.resale.ResaleStatus;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import play.cache.Cache;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.libs.Images;
import play.mvc.Controller;
/**
 * 分销商注册
 * 
 * @author yanjy
 *
 */
public class Register extends Controller {

	/**
	 * 注册页面
	 */
	public static void index(){
		render();
	}

	/**
	 *  注册新用户
	 * 
	 * @param resaler 用户信息
	 */
	public static void create(@Valid Resale resaler) {
		if(!resaler.password.equals(resaler.confirmPassword)){
			Validation.addError("resaler.confirmPassword", "两次密码输入的不一样！！");
		}
		if (Validation.hasErrors()) {
			render("Register/index.html", resaler);
		}

		Images.Captcha captcha = Images.captcha();
		String passwordSalt=captcha.getText(6);
		//密码加密
		resaler.password=DigestUtils.md5Hex(resaler.password+passwordSalt);
		//正常
		resaler.status=ResaleStatus.NOMAL;
		//随机吗
		resaler.passwordSalt=passwordSalt;
		//获得IP
		resaler.loginIp=request.current().remoteAddress;
		resaler.lastLoginAt = new Date();
		resaler.createdAt = new Date();
		resaler.save();

		redirect("http://www.uhuiladev.com");
	}


}
