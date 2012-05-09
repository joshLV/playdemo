package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.consumer.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.modules.breadcrumbs.BreadcrumbList;
import play.mvc.Controller;
import play.mvc.With;

@With(SecureCAS.class)
public class UserPasswords extends Controller {

    /**
     * 修改密码页面
     */
    public static void index() {
        User user = SecureCAS.getUser();
        user.password = "";
        BreadcrumbList breadcrumbs = new BreadcrumbList("修改密码", "/user/password");
        render(user, breadcrumbs);
    }

    /**
     * 修改密码
     */
    public static void update(long id, User user) {
        BreadcrumbList breadcrumbs = new BreadcrumbList("修改密码", "/user/password");
        User newUser;
        if (id == 0) {
            newUser = SecureCAS.getUser();
        } else {
            newUser = User.findById(id);
        }
        //密码验证
        checkPassword(user, newUser);

        if (Validation.hasErrors()) {
            render("UserPasswords/index.html", user,breadcrumbs);
        }

        User.updatePassword(newUser, user);
        String isOk = "isOk";
        render("UserPasswords/index.html", user, isOk, breadcrumbs);
    }

    /**
     * 验证密码
     *
     * @param user    用户原密码信息
     * @param newUser 用户新密码信息
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
