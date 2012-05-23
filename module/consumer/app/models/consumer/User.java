package models.consumer;

import models.mail.CouponMessage;
import models.mail.MailUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.data.validation.Email;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.Images;
import play.modules.paginate.JPAExtPaginator;
import play.modules.view_ext.annotation.Mobile;
import play.mvc.Http.Request;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends Model {

    @Column(name = "email")
    @Required
    @Email
    public String loginName;

    @Mobile
    public String mobile;
    /**
     * 第三方登录帐号的来源系统
     */
    @Column(name = "openid_source")
    public String openIdSource;

    @Column(name = "encrypted_password")
    @Required
    @MinSize(value = 6)
    @MaxSize(value = 20)
    public String password;

    @Transient
    @Required
    public String confirmPassword;
    /**
     * 图形校验码
     */
    @Transient
    @Required
    public String captcha;

    @Column(name = "password_salt")
    public String passwordSalt;

    @Column(name = "last_login_at")
    public Date lastLoginAt;

    @Enumerated(EnumType.STRING)
    public UserStatus status;

    @Column(name = "login_ip")
    public String loginIp;

    @Column(name = "created_at")
    public Date createdAt;

    @Transient
    public String oldPassword;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public UserInfo userInfo;

    @Column(name = "password_token")
    public String passwordToken;

    @Column(name = "send_mail_at")
    public Date sendMailAt;

    /**
     * 判断用户名是否唯一
     *
     * @param loginName 用户名
     */
    public static boolean checkLoginName(String loginName) {

        List<User> userList = User.find("byLoginName", loginName).fetch();
        boolean isExisted = false;
        //用户名存在的情况
        if (userList.size() > 0) isExisted = true;
        return isExisted;
    }

    /**
     * 判断手机是否存在
     *
     * @param mobile 手机
     */
    public static boolean checkMobile(String mobile) {

        boolean isExisted = false;
        //手机存在的情况
        List<User> mList = User.find("byMobile", mobile).fetch();
        if (mList.size() > 0) isExisted = true;
        return isExisted;
    }

    /**
     * 判断用户名是否存在
     *
     * @param loginName 用户名
     * @return 用户名是否存在
     */
    public static boolean isExisted(String loginName) {
        boolean isExisted = false;

        User user = findByLoginName(loginName);
        //用户名存在的情况
        if (user != null) {
            isExisted = true;
            String token = user.id + loginName;
            token = DigestUtils.md5Hex(token);
            user.passwordToken = token;
            user.sendMailAt = new Date();
            user.save();
            //发送邮件
            CouponMessage mail = new CouponMessage();
            String url = Play.configuration.getProperty("resetpassword.mail_url");
            mail.setMailUrl(url + "?token=" + token);
            mail.setEmail(loginName);
            MailUtil.sendFindPasswordMail(mail);
        }
        return isExisted;
    }

    public static User findByLoginName(String loginName) {
        return User.find("byLoginName", loginName).first();
    }

    /**
     * 修改密码
     *
     * @param newUser 新密码信息
     * @param user    原密码信息
     */
    public static void updatePassword(User newUser, User user) {
        // 随机码
        Images.Captcha captcha = Images.captcha();
        String newPasswordSalt = captcha.getText(6);
        newUser.passwordSalt = newPasswordSalt;
        // 新密码
        String newPassword = user.password;
        newUser.password = DigestUtils.md5Hex(newPassword + newPasswordSalt);
        newUser.save();

    }

    /**
     * 创建.
     */
    @Override
    public boolean create() {
        Images.Captcha captcha = Images.captcha();
        String salt = captcha.getText(6);
        //密码加密
        password = DigestUtils.md5Hex(password + salt);
        //正常
        status = UserStatus.NORMAL;
        //随机码
        passwordSalt = salt;
        //获得IP
        loginIp = Request.current().remoteAddress;
        lastLoginAt = new Date();
        createdAt = new Date();
        return super.create();

    }

    /**
     * 更新手机
     *
     * @param mobile 手机
     */
    public void updateMobile(String mobile) {
        this.mobile = mobile;
        this.userInfo.mobile = mobile;
        this.userInfo.bindMobileAt = new Date();
        this.userInfo.save();
        this.save();
    }

    /**
     * 根据邮箱或手机更新密码
     *
     * @param token    邮箱的
     * @param mobile
     * @param password
     */
    public static void updateFindPwd(String token, String mobile, String password) {
        User user = null;
        if (!StringUtils.isBlank(token)) {
            user = User.find("byPasswordToken", token).first();
        }
        if (!StringUtils.isBlank(mobile)) {
            user = User.find("byMobile", mobile).first();
        }
        user.password = password;
        // 随机码
        Images.Captcha captcha = Images.captcha();
        String newPasswordSalt = captcha.getText(6);
        user.passwordSalt = newPasswordSalt;
        user.password = DigestUtils.md5Hex(password + newPasswordSalt);
        user.save();


    }

    /**
     * 判断链接是否超过24小时
     *
     * @param token 标识
     * @return 过期标识
     */
    public static boolean isExpired(String token) {
        boolean isExpired = false;
        if (!StringUtils.isBlank(token)) {
            System.out.println(token);
            User user = User.find("byPasswordToken", token).first();
            Date d1 = user.sendMailAt;
            Date d2 = new Date();
            long diff = d2.getTime() - d1.getTime();
            long hour = diff / (1000 * 60 * 60);
            //超过24小时，表示过期
            if (hour > 24) {
                isExpired = true;
            }
        }
        return isExpired;
    }

    public static JPAExtPaginator<User> findByCondition(UserCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<User> userPage = new JPAExtPaginator<>("User u", "u", User.class, condition.getFilter(),
                condition.getParamMap()).orderBy("u.lastLoginAt DESC");
        userPage.setPageNumber(pageNumber);
        userPage.setPageSize(pageSize);
        userPage.setBoundaryControlsEnabled(false);
        return userPage;
    }

    public static void freeze(long id) {
        updateStatus(id, UserStatus.FREEZE);
    }

    public static void unfreeze(long id) {
        updateStatus(id, UserStatus.NORMAL);
    }

    private static void updateStatus(long id, UserStatus status) {
        User user = User.findById(id);
        if (user == null) {
            return;
        }
        user.status = status;
        user.save();
    }
}
