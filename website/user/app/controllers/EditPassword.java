package controllers;

import java.util.List;

import models.Registers;

import org.apache.commons.codec.digest.DigestUtils;

import play.cache.Cache;
import play.db.jpa.GenericModel.JPAQuery;
import play.mvc.Controller;
import play.mvc.With;

import common.CharacterUtil;

import controllers.modules.cas.SecureCAS;

@With(SecureCAS.class)
public class EditPassword extends Controller{
	/**
	 * 用户修改密码页面
	 * 
	 * @return
	 */
	public static void index(){
		String username =session.get("username");
		render("Register/editpassword.html",username);
	}

	/**
	 * 
	 * 
	 * @param id
	 */
	public static void update(String username){
		//旧密码
		String oldloginpass=	params.get("oldloginpass");
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
			index();
		}
		
		Registers registers= null;
		List<Registers> list= Registers.find("email = ? ",""+username+"").fetch();
		//原密码随机码
		String old_password_salt="";
		//原密码
		String old_password="";
		
		if (list.size()>0) {			
			registers =list.get(0);		
			
			old_password_salt=registers.password_salt;
			
			old_password=registers.crypted_password;
			
			//验证输入的原密码是否一致
			oldloginpass=DigestUtils.md5Hex(oldloginpass+old_password_salt);
			
			validation.equals(oldloginpass,old_password).message("原密码不对，请重新输入！");
			
			if(validation.hasErrors()) {
				params.flash();
				validation.keep();
				index();
			}
			
			//验证通过后，更新新密码
			String password_salt= CharacterUtil.getRandomString(6);
			registers.crypted_password=DigestUtils.md5Hex(newloginpass+password_salt);
			registers.password_salt=password_salt;
			registers.save();
			flash.success("密码修改成功！");
			index();
		} else {
			flash.error("你的信息不存在！");
			index();
		}
	}
}
