package controllers;

import models.consumer.User;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.website.cas.SecureCAS;

@With(SecureCAS.class)
public class UserPassword extends Controller{

	/**
	 * 修改密码页面 
	 */
	public static void index() {
		User user = SecureCAS.getUser();
		user.password = "";
		render(user);
	}

	/**
	 * 修改密码
	 */
	public static void update(long id, User user) {
		User newUser = User.findById(id);
		if(id == 0 ) {
			newUser = SecureCAS.getUser();
		} else {
			 newUser = User.findById(id);
		}
		//密码验证
		checkPassword(user, newUser);

		if (Validation.hasErrors()) {
			render("UserPassword/index.html", user);
		}

		User.updatePassword(newUser, user);
		String isOk = "isOk";
		render("UserPassword/index.html", user, isOk);
	}

	/**
	 * 验证密码
	 * 
	 * @param user 用户新密码信息
	 *            
	 * @param oldResaler 用户原密码信息
	 *            
	 */
	private static void checkPassword(User user, User newUser) {
		// 新密码
		String newPassword = user.password;
		Validation.required("user.newPassword", newPassword);
		Validation.required("user.newConfirmPassword",
				user.confirmPassword);
		Validation.required("user.oldPassword", user.oldPassword);

		if (StringUtils.isNotBlank(newPassword) && newPassword.length() < 6) {
			Validation.addError("user.newPassword", "validation.newPassword.minSize");
		}
		if (StringUtils.isNotBlank(newPassword) && newPassword.length() > 20) {
			Validation.addError("user.newPassword", "validation.newPassword.maxSize");
		}
		// 新密码比较
		if (!StringUtils.isNotBlank(newPassword)
				&& newPassword.equals(user.confirmPassword)) {
			Validation.addError("user.confirmPassword", "validation.confirmPassword");
		}

		// 加密后的原密码比较
		String oldPassword = DigestUtils.md5Hex(user.oldPassword
				+ newUser.passwordSalt);
		if (!StringUtils.normalizeSpace(oldPassword)
				.equals(newUser.password)) {
			Validation.addError("user.oldPassword", "validation.oldPassword");
		}

		if (StringUtils.normalizeSpace(user.oldPassword)
				.equals(newPassword)) {
			Validation.addError("user.newPassword", "validation.newPassword.confirm");
		}
	}
}
