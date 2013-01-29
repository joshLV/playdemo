package controllers;

import controllers.supplier.SupplierInjector;
import models.admin.SupplierUser;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 商户用户修改密码.
 * <p/>
 * User: sujie
 * Date: 1/29/13
 * Time: 9:38 AM
 */
@With({SupplierRbac.class, SupplierInjector.class})
public class SupplierUserPasswords extends Controller {

    /**
     * 修改密码页面
     */

    public static void index() {
        render();
    }

    /**
     * 修改密码
     */
    public static void update(String oldPassword, String password, String confirmPassword) {
        SupplierUser newSupplierUser = SupplierUser.findByUnDeletedId(SupplierRbac.currentUser().id);

        //密码验证
        checkPassword(newSupplierUser, oldPassword, password, confirmPassword);

        if (Validation.hasErrors()) {
            render("SupplierUserPasswords/index.html", oldPassword, password, confirmPassword);
        }

        SupplierUser.updatePassword(newSupplierUser, password);
        String successInfo = "密码修改成功。";
        render("SupplierUserPasswords/index.html", successInfo);
    }

    /**
     * 验证密码
     */
    private static void checkPassword(SupplierUser newSupplierUser, String oldPassword, String newPassword, String confirmPassword) {
        // 新密码
        Validation.required("supplierUser.newPassword", newPassword);
        Validation.required("supplierUser.confirmPassword",confirmPassword);
        Validation.required("supplierUser.oldPassword", oldPassword);

        if (StringUtils.isNotBlank(newPassword) && newPassword.length() < 6) {
            Validation.addError("supplierUser.newPassword", "validation.newPassword.minSize");
        }
        if (StringUtils.isNotBlank(newPassword) && newPassword.length() > 20) {
            Validation.addError("supplierUser.newPassword", "validation.newPassword.maxSize");
        }
        // 加密后的原密码比较
        String encryptedPassword = DigestUtils.md5Hex(oldPassword
                + newSupplierUser.passwordSalt);
        if (!encryptedPassword.equals(newSupplierUser.encryptedPassword)) {
            Validation.addError("supplierUser.oldPassword", "validation.oldPassword");
        }
        // 新密码比较
        if (StringUtils.isNotBlank(newPassword)
                && !newPassword.equals(confirmPassword)) {
            Validation.addError("supplierUser.confirmPassword", "validation.passwordDiff");
        }

        //新密码和旧密码不能一样
        if (StringUtils.isNotBlank(oldPassword)
                && oldPassword.equals(newPassword)) {
            Validation.addError("supplierUser.newPassword", "validation.newPassword.confirm");
        }
    }
}
