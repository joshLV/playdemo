package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.consumer.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.modules.breadcrumbs.BreadcrumbList;
import play.mvc.Controller;
import play.mvc.With;

@With({SecureCAS.class, WebsiteInjector.class})
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
    public static void update(String oldPassword, String password, String confirmPassword) {
        BreadcrumbList breadcrumbs = new BreadcrumbList("修改密码", "/user/password");
        User newUser = SecureCAS.getUser();
        //密码验证
        checkPassword(newUser, oldPassword, password, confirmPassword);

        if (Validation.hasErrors()) {
            render("UserPasswords/index.html", oldPassword, password, confirmPassword, breadcrumbs);
        }

        User.updatePassword(newUser, password);
        renderArgs.put("isOk", true);
        render("UserPasswords/index.html", breadcrumbs);
    }

    /**
     * 验证密码
     *
     * @param newUser 用户新密码信息
     */
    private static void checkPassword(User newUser, String oldPassword, String newPassword, String confirmPassword) {
        // 新密码
        Validation.required("user.newPassword", newPassword);
        Validation.required("user.confirmPassword", confirmPassword);
        Validation.required("user.oldPassword", oldPassword);

        if (StringUtils.isNotBlank(newPassword) && newPassword.length() < 6) {
            Validation.addError("user.newPassword", "validation.newPassword.minSize");
        }
        if (StringUtils.isNotBlank(newPassword) && newPassword.length() > 20) {
            Validation.addError("user.newPassword", "validation.newPassword.maxSize");
        }
        // 加密后的原密码比较
        String encryptedPassword = DigestUtils.md5Hex(oldPassword
                + newUser.passwordSalt);
        if (!encryptedPassword.equals(newUser.password)) {
            Validation.addError("user.oldPassword", "validation.oldPassword");
        }
        // 新密码比较
        if (StringUtils.isNotBlank(newPassword)
                && !newPassword.equals(confirmPassword)) {
            Validation.addError("user.confirmPassword", "validation.passwordDiff");
        }

        //新密码和旧密码不能一样
        if (StringUtils.isNotBlank(oldPassword)
                && oldPassword.equals(newPassword)) {
            Validation.addError("user.newPassword", "validation.newPassword.confirm");
        }

    }
}
