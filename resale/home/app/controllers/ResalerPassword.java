package controllers;

import controllers.modules.resale.cas.SecureCAS;
import models.resale.Resaler;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 分销商信息管理中心
 *
 * @author yanjy
 */
@With(SecureCAS.class)
public class ResalerPassword extends Controller {

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
        Resaler newResaler = SecureCAS.getResaler();
        //密码验证
        checkPassword(newResaler, oldPassword, password, confirmPassword);
        if (Validation.hasErrors()) {
            render("ResalerPassword/index.html", oldPassword, password, confirmPassword);
        }

        Resaler.updatePassword(newResaler, password);
        boolean isOk = true;
        render("ResalerPassword/index.html", isOk);
    }

    /**
     * 验证密码
     *
     * @param newResaler
     * @param oldPassword
     * @param newPassword
     * @param confirmPassword
     */
    private static void checkPassword(Resaler newResaler, String oldPassword, String newPassword, String confirmPassword) {
        // 新密码
        Validation.required("resaler.newPassword", newPassword);
        Validation.required("resaler.confirmPassword", confirmPassword);
        Validation.required("resaler.oldPassword", oldPassword);

        if (StringUtils.isNotBlank(newPassword) && newPassword.length() < 6) {
            Validation.addError("resaler.newPassword", "validation.newPassword.minSize");
        }
        if (StringUtils.isNotBlank(newPassword) && newPassword.length() > 20) {
            Validation.addError("resaler.newPassword", "validation.newPassword.maxSize");
        }
        // 加密后的原密码比较
        String encryptedPassword = DigestUtils.md5Hex(oldPassword
                + newResaler.passwordSalt);
        if (!encryptedPassword.equals(newResaler.password)) {
            Validation.addError("resaler.oldPassword", "validation.oldPassword");
        }
        // 新密码和确认密码比较
        if (StringUtils.isNotBlank(newPassword)
                && !newPassword.equals(confirmPassword)) {
            Validation.addError("resaler.confirmPassword", "validation.confirmPassword");
        }
        //新密码和旧密码不能一样
        if (StringUtils.isNotBlank(oldPassword)
                && oldPassword.equals(newPassword)) {
            Validation.addError("resaler.newPassword", "validation.newPasswordIsTheSameWithOldPassword");
        }


    }
}
