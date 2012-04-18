package controllers;

import controllers.modules.resale.cas.SecureCAS;
import models.consumer.User;
import models.resale.Resaler;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.libs.Images;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 分销商信息管理中心
 * 
 * @author yanjy
 * 
 */
@With(SecureCAS.class)
public class ResalerPassword extends Controller {

	/**
	 * 修改密码页面
	 */
	public static void index() {
		Resaler resaler = SecureCAS.getResaler();
		resaler.password = "";
		render(resaler);
	}

	/**
	 * 修改密码
	 */
	public static void update(long id, Resaler resaler) {
		Resaler newResaler = null;
		if(id == 0 ) {
			newResaler = SecureCAS.getResaler();
		} else {
			newResaler = Resaler.findById(id);
		}
		//密码验证
		checkPassword(resaler, newResaler);

		if (Validation.hasErrors()) {
			render("ResalerPassword/index.html", resaler);
		}

		Resaler.updatePassword(newResaler, resaler);
		String isOk = "isOk";
		render("ResalerPassword/index.html", resaler, isOk);
	}

	/**
	 * 验证密码
	 * 
	 * @param resaler 新分销商密码信息
	 *            
	 * @param oldResaler 原分销商密码信息
	 *            
	 */
	private static void checkPassword(Resaler resaler, Resaler newResaler) {
		// 新密码
		String newPassword = resaler.password;
		Validation.required("resaler.newPassword", newPassword);
		Validation.required("resaler.newConfirmPassword",
				resaler.confirmPassword);
		Validation.required("resaler.oldPassword", resaler.oldPassword);

		if (StringUtils.isNotBlank(newPassword) && newPassword.length() < 6) {
			Validation.addError("resaler.newPassword", "validation.newPassword.minSize");
		}
		if (StringUtils.isNotBlank(newPassword) && newPassword.length() > 20) {
			Validation.addError("resaler.newPassword", "validation.newPassword.maxSize");
		}
		// 新密码比较
		if (!StringUtils.isNotBlank(newPassword)
				&& newPassword.equals(resaler.confirmPassword)) {
			Validation.addError("resaler.confirmPassword", "validation.confirmPassword");
		}

		// 加密后的原密码比较
		String oldPassword = DigestUtils.md5Hex(resaler.oldPassword
				+ newResaler.passwordSalt);
		if (!StringUtils.normalizeSpace(oldPassword)
				.equals(newResaler.password)) {
			Validation.addError("resaler.oldPassword", "validation.oldPassword");
		}

		if (StringUtils.normalizeSpace(resaler.oldPassword)
				.equals(newPassword)) {
			Validation.addError("resaler.newPassword", "validation.newPassword.confirm");
		}
	}
}
