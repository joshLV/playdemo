package models.consumer;

import com.uhuila.common.util.RandomNumberUtil;
import models.accounts.Account;
import models.accounts.util.AccountUtil;
import models.mail.MailMessage;
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

    private static final long serialVersionUID = 812320609113062L;

    @Column(name = "email")
    @Required
    @Email
    public String loginName;

    @Mobile
    public String mobile;
    /**
     * 第三方登录帐号的来源系统
     */
    @Column(name = "open_id_source")
    @Enumerated(EnumType.STRING)
    public OpenIdSource openIdSource;

    @Column(name = "open_id")
    public String openId;

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
     * 用户唯一的推荐码
     */
    @Column(name = "promoter_code")
    public String promoterCode;
    /**
     * 推荐者ID
     */
    @Column(name = "promote_user_id", nullable = true)
    public Long promoteUserId;

    @Transient
    private Account account;

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
        //手机存在的情况
        List<User> mList = User.find("byMobile", mobile).fetch();
        return mList.size() > 0;
    }

    /**
     * 根据推荐码用户是否存在
     *
     * @param promoterCode 推荐码
     */
    public static User getUserByPromoterCode(String promoterCode) {
        //推荐码存在的情况
        return User.find("byPromoterCode", promoterCode.toLowerCase()).first();
    }

    /**
     * 判断第三方登录帐号是否存在
     *
     * @param openIdSource 第三方登录的应用
     * @param openId       第三方应用的ID
     * @return 第三方登录帐号是否存在
     */
    public static boolean checkOpenId(OpenIdSource openIdSource, String openId) {
        return User.count("byOpenIdSourceAndOpenId", openIdSource, openId) > 0;
    }

    /**
     * 判断用户名是否存在
     *
     * @param loginName 用户名
     * @return 用户名是否存在
     */
    public static boolean checkAndSendEmail(String loginName) {
        boolean isExisted = false;
        User user = findByLoginName(loginName);
        //用户名存在的情况
        if (user != null) {
            isExisted = true;
            String token = user.id + loginName + System.currentTimeMillis();
            token = DigestUtils.md5Hex(token);
            user.passwordToken = token;
            user.sendMailAt = new Date();
            user.save();
            //发送邮件
            MailMessage mail = new MailMessage();
            String url = Play.configuration.getProperty("resetpassword.mail_url");
            mail.putParam("mail_url", url + "?token=" + token);
            mail.addRecipient(loginName);
            MailUtil.sendFindPasswordMail(mail);
        }
        return isExisted;
    }

    public static User findByLoginName(String loginName) {
        if (User.isOpenIdExpress(loginName)) {
            return User.find("byOpenIdSourceAndOpenId", User.getOpenSourceFromName(loginName), User.getOpenIdFromName(loginName)).first();
        }
        return User.find("byLoginName", loginName).first();
    }

    /**
     * 修改密码
     *
     * @param newUser  新密码信息
     * @param password 密码
     */
    public static void updatePassword(User newUser, String password) {
        // 随机码
        Images.Captcha captcha = Images.captcha();
        String newPasswordSalt = captcha.getText(6);
        newUser.passwordSalt = newPasswordSalt;
        // 新密码
        newUser.password = DigestUtils.md5Hex(password + newPasswordSalt);
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
        this.loginName = this.loginName.toLowerCase().trim();
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
//        this.userInfo.bindMobileAt = new Date();
//        this.userInfo.save();
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
            User user = User.find("byPasswordToken", token).first();
            if (user == null) {
                isExpired = true; // 已过期
                return isExpired;
            }
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

    public static final int SHOW_NAME_LIMIT = 10;

    @Transient
    public String getShowName() {
        if (StringUtils.isBlank(loginName)) {
            return getOpenIdExpress().length() > SHOW_NAME_LIMIT ? getOpenIdExpress().substring(0, SHOW_NAME_LIMIT) + "..." : getOpenIdExpress();
        }
        return loginName;
    }

    @Transient
    public String getOpenIdExpress() {
        return getOpenIdSourceExpress() + openId;
    }

    public static final String SOURCE_SINA_WEIBO = "新浪微博用户";
    public static final String SOURCE_QQ = "QQ用户";
    public static final String SOURCE_RENREN = "人人网用户";
    public static final String SOURCE_OTHERS = "第三方用户";

    private String getOpenIdSourceExpress() {
        if (openIdSource == null) {
            return "";
        }
        switch (openIdSource) {
            case SinaWeibo:
                return SOURCE_SINA_WEIBO;
            case QQ:
                return SOURCE_QQ;
            case RenRen:
                return SOURCE_RENREN;
            default:
                return SOURCE_OTHERS;
        }
    }

    /**
     * 产生唯一的推荐码
     */
    public void generatePromoterCode() {
        String promoteCode = this.promoterCode;
        if (StringUtils.isBlank(promoteCode)) {
            do {
                promoteCode = RandomNumberUtil.generateRandomNumber(6);
            } while (User.getUserByPromoterCode(promoteCode) != null);
            this.promoterCode = promoteCode.toLowerCase().trim();
            this.save();
        }

    }

    /**
     * 访问account表获取
     *
     * @return
     */
    @Transient
    public Account getAccount() {
        if (account != null) {
            return account;
        }
        account = AccountUtil.getConsumerAccount(id);
        return account;
    }

    /**
     * 根据用户名字判断是否第三方登录用户.
     */
    public static boolean isOpenIdExpress(String name) {
        return name.startsWith(SOURCE_SINA_WEIBO) || name.startsWith(SOURCE_QQ) || name.startsWith(SOURCE_RENREN) ||
                name.startsWith(SOURCE_OTHERS) || name.contains("用户");
    }

    /**
     * 根据给出的第三方登录名字返回第三方.
     *
     * @param name
     * @return
     */
    public static OpenIdSource getOpenSourceFromName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        if (name.startsWith(SOURCE_SINA_WEIBO)) {
            return OpenIdSource.SinaWeibo;
        } else if (name.startsWith(SOURCE_RENREN)) {
            return OpenIdSource.RenRen;
        } else if (name.startsWith(SOURCE_QQ)) {
            return OpenIdSource.QQ;
        }
        return null;
    }

    /**
     * 根据给出的第三方登录名字返回第三方登录ID.
     *
     * @param name
     * @return
     */
    public static String getOpenIdFromName(String name) {
        String openId = "";
        int start = name.indexOf("用户");
        if (start >= 0) {
            openId = name.substring(start + 2, name.length());
        }
        if (openId.endsWith("...")) {
            openId = openId.substring(0, openId.indexOf("..."));
        }
        return openId;
    }
}
