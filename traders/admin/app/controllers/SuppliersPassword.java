package controllers;

import models.admin.SupplierUser;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 商户信息管理中心
 * 
 * @author yanjy
 * 
 */
@With(SupplierRbac.class)
public class SuppliersPassword extends Controller {

	/**
	 * 修改密码页面
	 */

	public static void edit() {
		SupplierUser supplierUser = SupplierUser.findByUnDeletedId(SupplierRbac.currentUser().id);
		supplierUser.encryptedPassword = "";
		render(supplierUser);
	}

	/**
	 * 修改密码
	 */
	public static void update(SupplierUser supplierUser) {
		SupplierUser newSupplierUser = SupplierUser.findByUnDeletedId(SupplierRbac.currentUser().id);

		//密码验证
		checkPassword(supplierUser, newSupplierUser);

		if (Validation.hasErrors()) {
			render("SuppliersPassword/edit.html", supplierUser);
		}

		SupplierUser.updatePassword(newSupplierUser, supplierUser);
		String isOk = "isOk";
		render("SuppliersPassword/edit.html", supplierUser, isOk);
	}

	/**
	 * 验证密码
	 *
	 */
	private static void checkPassword(SupplierUser supplierUser, SupplierUser newSupplierUser) {
		// 新密码
		String newPassword = supplierUser.encryptedPassword;
		Validation.required("supplierUser.newPassword", newPassword);
		Validation.required("supplierUser.newConfirmPassword",
				supplierUser.confirmPassword);
		Validation.required("supplierUser.oldPassword", supplierUser.oldPassword);

		if (StringUtils.isNotBlank(newPassword) && newPassword.length() < 6) {
			Validation.addError("supplierUser.newPassword", "validation.newPassword.minSize");
		}
		if (StringUtils.isNotBlank(newPassword) && newPassword.length() > 20) {
			Validation.addError("supplierUser.newPassword", "validation.newPassword.maxSize");
		}
		// 新密码比较
		if (StringUtils.isNotBlank(newPassword)
				&& !newPassword.equals(supplierUser.confirmPassword)) {
			Validation.addError("supplierUser.newConfirmPassword", "validation.confirmPassword");
		}

		// 加密后的原密码比较
		String oldPassword = DigestUtils.md5Hex(supplierUser.oldPassword
				+ newSupplierUser.passwordSalt);
		if (!StringUtils.normalizeSpace(oldPassword)
				.equals(newSupplierUser.encryptedPassword)) {
			Validation.addError("supplierUser.oldPassword", "validation.oldPassword");
		}

		if (StringUtils.normalizeSpace(supplierUser.oldPassword)
				.equals(newPassword)) {
			Validation.addError("supplierUser.newPassword", "validation.newPassword.confirm");
		}
	}
}
