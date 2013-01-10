package controllers;

import models.admin.OperateUser;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Error;
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
        OperateUser operateUser = OperateUser.findById(OperateRbac.currentUser().id);
        operateUser.encryptedPassword = "";
        render(operateUser);
    }

    /**
     * 修改密码
     */
    public static void update(OperateUser operateUser) {
        OperateUser currentOperateUser = OperateUser.findById(OperateRbac.currentUser().id);

        //密码验证
        checkPassword(operateUser, currentOperateUser);

        if (Validation.hasErrors()) {
            render("OperateUsersPassword/index.html", operateUser);
        }

        OperateUser.updatePassword(currentOperateUser, operateUser);
        String isOk = "isOk";

        render("OperateUsersPassword/index.html", operateUser, isOk);
    }

    /**
     * 验证密码
     */
    private static void checkPassword(OperateUser operateUser, OperateUser newOperateUser) {
        // 新密码
        String newPassword = operateUser.encryptedPassword;
        Validation.required("operateUser.newPassword", newPassword);
        Validation.required("operateUser.newConfirmPassword",
                operateUser.confirmPassword);
        Validation.required("operateUser.oldPassword", operateUser.oldPassword);

        if (StringUtils.isNotBlank(newPassword) && newPassword.length() < 6) {
            Validation.addError("operateUser.newPassword", "validation.newPassword.minSize");
        }
        if (StringUtils.isNotBlank(newPassword) && newPassword.length() > 20) {
            Validation.addError("operateUser.newPassword", "validation.newPassword.maxSize");
        }

        // 新密码比较
        if (StringUtils.isNotBlank(newPassword)
                && !newPassword.equals(operateUser.confirmPassword)) {
            Validation.addError("operateUser.newConfirmPassword", "validation.confirmPassword");
        }

        // 加密后的原密码比较
        String oldPassword = DigestUtils.md5Hex(operateUser.oldPassword
                + newOperateUser.passwordSalt);
        if (!StringUtils.normalizeSpace(oldPassword)
                .equals(newOperateUser.encryptedPassword)) {
            Validation.addError("operateUser.oldPassword", "validation.oldPassword");
        }

        if (StringUtils.normalizeSpace(operateUser.oldPassword)
                .equals(newPassword)) {
            Validation.addError("operateUser.newPassword", "validation.newPassword.confirm");
        }
    }
}
