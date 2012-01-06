package controllers;

import java.util.List;

import models.Cusers;

import org.apache.commons.codec.digest.DigestUtils;

import play.cache.Cache;
import play.db.jpa.GenericModel.JPAQuery;
import play.libs.Codec;
import play.libs.Images;
import play.mvc.Controller;
import play.mvc.With;

import common.CharacterUtil;

import controllers.modules.cas.SecureCAS;

@With(SecureCAS.class)
public class CusersApp extends Controller{
	
	/**
	 *注册 用户一览
	 */
	public static void index() {
		String login_name = params.get("login_name");
		JPAQuery query = null;
		List<Cusers> list =null;
		if(login_name != null && !"".equals(login_name.trim()) ){
		   query=Cusers.find("login_name like ? ", "%"+login_name+"%");
		   list=query.fetch();
		}else{
		   list=Cusers.findAll();
		}
		render(list);
		
	}
	/**
	 * 用户修改密码页面
	 * 
	 * @return
	 */
	public static void view(Long id){
		render("CusersApp/view.html",id);
	}
	/**
	 * UUID
	 * 
	 * @return
	 */
	public static String genRandomId(){
		return Codec.UUID();
	}
	
	/**
	 * 验证码
	 * @param id
	 */
	public static void captcha(String randomID) {
		Images.Captcha captcha = Images.captcha();
		String code = captcha.getText("#49B07F",4);
		Cache.set(randomID, code, "30mn");
		renderBinary(captcha);
	}

	/**
	 * 密码修改
	 * @param id
	 */
	public static void update(Long id){
		//旧密码
		String oldloginpass=params.get("oldloginpass");
		//新密码
		String newloginpass=params.get("newloginpass");
		//新确认密码
		String againloginpass=params.get("againloginpass");
		//验证码
		String captcha =params.get("captcha");
		validation.required(oldloginpass);
		validation.required(newloginpass);
		validation.required(againloginpass);
		validation.equals(againloginpass, newloginpass).message("两次密码输入的不一样！！");
		validation.required(captcha);
		if(validation.hasErrors()) {
			params.flash();
			validation.keep();
			render("CusersApp/view.html",params,id);
		}

		Cusers cusers= Cusers.findById(id);
		String old_password_salt=cusers.randnum;
		String old_password=cusers.password;
		oldloginpass=DigestUtils.md5Hex(oldloginpass+old_password_salt);
		System.out.println(oldloginpass);
		validation.equals(oldloginpass,old_password).message("原密码不对，请重新输入！");
		if(validation.hasErrors()) {
			params.flash();
			validation.keep();
			render("CusersApp/view.html",params,id);
		}
		String randnum= CharacterUtil.getRandomString(6);
		cusers.password=DigestUtils.md5Hex(newloginpass+randnum);
		cusers.randnum=randnum;
		cusers.save();
		index();
	}
}
