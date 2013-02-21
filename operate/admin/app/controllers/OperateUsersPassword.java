package controllers;

import models.operator.OperateUser;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 运营后台
 * <p/>
 * User: yanjy
 * Date: 12-6-1
 * Time: 上午10:16
 */
@With(OperateRbac.class)
public class OperateUsersPassword extends Controller {

    public static void index() {
        render();
    }

    /**
     * 修改密码
     */
    public static void update(String oldPassword, String password, String confirmPassword) {
        OperateUser currentOperateUser = OperateUser.findById(OperateRbac.currentUser().id);

        //密码验证
        checkPassword(currentOperateUser, oldPassword, password, confirmPassword);

        if (Validation.hasErrors()) {
            render("OperateUsersPassword/index.html", oldPassword, password, confirmPassword);
        }

        OperateUser.updatePassword(currentOperateUser, password);
        boolean isOk = true;

        render("OperateUsersPassword/index.html", isOk);
    }

    /**
     * 验证密码
     */
    private static void checkPassword(OperateUser newOperateUser, String oldPassword, String newPassword, String confirmPassword) {
        // 新密码
        Validation.required("operateUser.newPassword", newPassword);
        Validation.required("operateUser.confirmPassword", confirmPassword);
        Validation.required("operateUser.oldPassword", oldPassword);

        if (StringUtils.isNotBlank(newPassword) && newPassword.length() < 6) {
            Validation.addError("operateUser.newPassword", "validation.newPassword.minSize");
        }
        if (StringUtils.isNotBlank(newPassword) && newPassword.length() > 20) {
            Validation.addError("operateUser.newPassword", "validation.newPassword.maxSize");
        }

        // 加密后的原密码比较
        String encryptedPassword = DigestUtils.md5Hex(oldPassword
                + newOperateUser.passwordSalt);
        if (!encryptedPassword.equals(newOperateUser.encryptedPassword)) {
            Validation.addError("operateUser.oldPassword", "validation.oldPassword");
        }
        // 新密码比较
        if (StringUtils.isNotBlank(newPassword)
                && !newPassword.equals(confirmPassword)) {
            Validation.addError("operateUser.confirmPassword", "validation.confirmPassword");
        }

        //新密码和旧密码不能一样
        if (StringUtils.isNotBlank(oldPassword)
                && oldPassword.equals(newPassword)) {
            Validation.addError("operateUser.newPassword", "validation.newPassword.confirm");
        }

    }
}
