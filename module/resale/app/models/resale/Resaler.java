package models.resale;

import models.accounts.Account;
import models.accounts.AccountCreditable;
import models.accounts.util.AccountUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import play.data.validation.Email;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Phone;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.Images;
import play.modules.paginate.JPAExtPaginator;
import play.modules.view_ext.annotation.Mobile;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "resaler")
public class Resaler extends Model {

    private static final long serialVersionUID = 16323229113062L;

    public static final String JD_LOGIN_NAME = "jingdong";
    public static final String DD_LOGIN_NAME = "dangdang";
    public static final String YHD_LOGIN_NAME = "yihaodian";

    /**
     * 分销商账户类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type")
    public AccountType accountType;

    /**
     * 是否可欠款
     */
    @Enumerated(EnumType.STRING)
    public ResalerCreditable creditable;

    @Column(name = "login_name")
    @Required
    @MinSize(value = 1)
    @MaxSize(value = 50)
    public String loginName;

    @Column(name = "encrypted_password")
    @Required
    @MinSize(value = 6)
    @MaxSize(value = 20)
    public String password;

    /**
     * 分销商联系人姓名
     */
    @Column(name = "user_name")
    @Required
    @MaxSize(value = 255)
    public String userName;

    @Transient
    @Required
    public String confirmPassword;

    @Column(name = "password_salt")
    public String passwordSalt;

    @Required
    @Mobile
    @MinSize(value = 11)
    public String mobile;

    @Required
    @Phone
    public String phone;

    @Column(name = "last_login_at")
    public Date lastLoginAt;

    @Column(name = "email")
    @Required
    @Email
    public String email;

    @Required
    public String address;

    @Column(name = "postcode")
    public String postCode;

    @Column(name = "identity_no")
    @Required
    public String identityNo;

    @Column(name = "app_key")
    @Index(name = "app_key")
    public String appKey;

    @Column(name = "app_secret_key")
    public String appSecretKey;

    /**
     * 分销商状态
     */
    @Enumerated(EnumType.STRING)
    public ResalerStatus status;

    /**
     * 分销商等级
     */
    @Enumerated(EnumType.STRING)
    public ResalerLevel level;

    @Column(name = "login_ip")
    public String loginIp;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;

    @MaxSize(value = 500)
    public String remark;

    @Transient
    public String oldPassword;

    public boolean isCreditable() {
        return this.creditable == ResalerCreditable.YES;
    }

    /**
     * 判断用户名和手机是否唯一
     *
     * @param loginName 用户名
     * @param mobile    手机
     */
    public static String checkValue(String loginName, String mobile) {

        List<Resaler> resalerList = Resaler.find("byLoginName", loginName).fetch();
        String returnFlag = "0";
        //用户名存在的情况
        if (resalerList.size() > 0) returnFlag = "1";
        else {
            //手机存在的情况
            List<Resaler> mList = Resaler.find("byMobile", mobile).fetch();
            if (mList.size() > 0) returnFlag = "2";
        }

        return returnFlag;
    }

    /**
     * 分销商一览
     *
     * @param condition  查询条件
     * @param pageNumber 页数
     * @param pageSize   记录数
     * @return
     */
    public static JPAExtPaginator<Resaler> findByCondition(ResalerCondition condition,
                                                           int pageNumber, int pageSize) {
        if (condition == null) {
            condition = new ResalerCondition();
        }

        JPAExtPaginator<Resaler> resalers = new JPAExtPaginator<>
                ("Resaler r", "r", Resaler.class, condition.getFitter(), condition.getParamMap())
                .orderBy("createdAt DESC");
        resalers.setPageNumber(pageNumber);
        resalers.setPageSize(pageSize);
        return resalers;
    }

    /**
     * 审核分销商
     *
     * @param id     分销商ID
     * @param status 状态
     * @param remark 备注
     */
    public static void update(Long id, ResalerStatus status, ResalerLevel level, String remark, ResalerCreditable creditable) {
        Resaler resaler = Resaler.findById(id);
        if (status != null) resaler.status = status;
        if (level != null) resaler.level = level;
        if (StringUtils.isNotEmpty(remark)) resaler.remark = remark;

        //修改现金账户是否可欠款
        if (creditable != null) {
            resaler.creditable = creditable;
            Account account = AccountUtil.getResalerAccount(resaler.getId());
            if (resaler.isCreditable()) {
                account.creditable = AccountCreditable.YES;
            } else {
                account.creditable = AccountCreditable.NO;
            }
            account.save();
        }
        resaler.save();
    }

    public static void freeze(long id) {
        update(id, ResalerStatus.FREEZE, null, null, null);
    }

    public static void unfreeze(long id) {
        update(id, ResalerStatus.APPROVED, null, null, null);
    }

    /**
     * 修改密码
     *
     * @param newResaler 新密码信息
     * @param resaler    原密码信息
     */
    public static void updatePassword(Resaler newResaler, Resaler resaler) {
        // 随机码
        Images.Captcha captcha = Images.captcha();
        String newPasswordSalt = captcha.getText(6);
        newResaler.passwordSalt = newPasswordSalt;
        // 新密码
        String newPassword = resaler.password;
        newResaler.password = DigestUtils.md5Hex(newPassword + newPasswordSalt);
        newResaler.save();

    }

    /**
     * 修改分销商信息
     *
     * @param id      ID
     * @param resaler 分销商信息
     */
    public static void updateInfo(Long id, Resaler resaler) {
        Resaler updResaler = Resaler.findById(id);
        updResaler.address = resaler.address;
        updResaler.mobile = resaler.mobile;
        updResaler.phone = resaler.phone;
        updResaler.email = resaler.email;
        updResaler.userName = resaler.userName;
        updResaler.postCode = resaler.postCode;
        updResaler.updatedAt = new Date();
        updResaler.save();
    }

    public static Resaler findOneByLoginName(String loginName) {
        return find("loginName = ?", loginName.trim()).first();
    }
}
