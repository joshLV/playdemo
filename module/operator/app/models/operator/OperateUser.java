package models.operator;

import cache.CacheHelper;
import com.uhuila.common.constants.DeletedStatus;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.Match;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.Images;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "operate_users")
public class OperateUser extends Model {

    private static final long serialVersionUID = 21943311362L;

    @Column(name = "login_name")
    @Required
    public String loginName;

    @Required
    @Match(value = "^1[3|4|5|8][0-9]\\d{4,8}$", message = "手机格式不对！")
    public String mobile;

    @Column(name = "encrypted_password")
    @Required
    @MinSize(value = 6)
    @MaxSize(value = 20)
    public String encryptedPassword;

    @Column(name = "password_salt")
    public String passwordSalt;

    @Column(name = "last_login_ip")
    public String lastLoginIP;

    @Column(name = "last_login_at")
    public Date lastLoginAt;

    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;


    @Column(name = "user_name")
    @Required
    public String userName;

    @Required
    @Match(value = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*", message = "邮箱格式不对！")
    public String email;

    @Required
    @Match(value = "^[0-9]*$", message = "工号格式不对!(需纯数字)")
    @Column(name = "job_number")
    public String jobNumber;


    @Transient
    public String confirmPassword;
    @Transient
    public String oldPassword;


    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;


    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "operate_users_roles",
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            joinColumns = @JoinColumn(name = "user_id"))
    public List<OperateRole> roles;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "operate_permissions_users",
            inverseJoinColumns = @JoinColumn(name = "permission_id"),
            joinColumns = @JoinColumn(name = "user_id"))
    public Set<OperatePermission> permissions;


    /**
     * 注意！
     * 这个值如果要修改，请保持与 CASPlugin.CACHEKEY 中的值一致
     */
    public static final String CACHEKEY = "OPUSER";

    @Override
    public void _save() {
        CacheHelper.delete(CACHEKEY + this.id);
        super._save();
    }

    @Override
    public void _delete() {
        CacheHelper.delete(CACHEKEY + this.id);
        super._delete();
    }

    /**
     * 查询操作员信息
     *
     * @param loginName  用户名
     * @param pageNumber 页数
     * @param pageSize   记录数
     * @return 操作员信息
     */
    public static JPAExtPaginator<OperateUser> getSupplierUserList(String loginName,
                                                                   int pageNumber, int pageSize) {
        StringBuffer sql = new StringBuffer();
        Map params = new HashMap();

        sql.append("s.deleted = :deleted ");
        params.put("deleted", DeletedStatus.UN_DELETED);

        if (StringUtils.isNotBlank(loginName)) {
            sql.append(" and s.loginName like :loginName");
            params.put("loginName", loginName + "%");
        }

        JPAExtPaginator<OperateUser> supplierUsersPage = new JPAExtPaginator<>("OperateUser s", "s",
                OperateUser.class, sql.toString(), params).orderBy("s.createdAt desc");
        supplierUsersPage.setPageNumber(pageNumber);
        supplierUsersPage.setPageSize(pageSize);
        supplierUsersPage.setBoundaryControlsEnabled(true);
        return supplierUsersPage;
    }

    /**
     * 更新操作员信息
     *
     * @param id   ID
     * @param user 用户信息
     */
    public static void update(long id, OperateUser user) {
        OperateUser updatedUser = OperateUser.findById(id);
        // FIX NullPointException for DigestUtils.md5Hex
        String updatedUser_encryptedPassword =
                StringUtils.isNotEmpty(updatedUser.encryptedPassword) ?
                        updatedUser.encryptedPassword : "!&NOTSETPASSWORD!";

        if (StringUtils.isNotEmpty(user.encryptedPassword) &&
                !"******".equals(user.encryptedPassword) &&
                !user.encryptedPassword.equals(DigestUtils.md5Hex(updatedUser_encryptedPassword))) {
            Images.Captcha captcha = Images.captcha();
            String passwordSalt = captcha.getText(6);
            //随机码
            updatedUser.passwordSalt = passwordSalt;
            updatedUser.encryptedPassword = DigestUtils.md5Hex(user.encryptedPassword + passwordSalt);
        }
        updatedUser.loginName = user.loginName;
        updatedUser.userName = user.userName;
        updatedUser.mobile = user.mobile;
        updatedUser.email = user.email;
        updatedUser.jobNumber = user.jobNumber;
        updatedUser.updatedAt = new Date();
        updatedUser.roles = user.roles;

        updatedUser.save();
    }

    /**
     * 判断用户名和手机是否唯一
     *
     * @param loginName 用户名
     * @param mobile    手机
     */
    public static String checkValue(Long id, String loginName, String mobile) {

        StringBuilder sq = new StringBuilder("loginName = ? ");
        List list = new ArrayList();
        list.add(loginName);
        if (id != null) {
            sq.append("and id <> ?");
            list.add(id);
        }
        List<OperateUser> supplierUserList = OperateUser.find(sq.toString(), list.toArray()).fetch();
        String returnFlag = "0";
        //用户名存在的情况
        if (supplierUserList.size() > 0) returnFlag = "1";
        else {
            sq = new StringBuilder("mobile = ? ");
            list = new ArrayList();
            list.add(mobile);
            if (id != null) {
                sq.append("and id <> ?");
                list.add(id);
            }
            //手机存在的情况
            List<OperateUser> mList = OperateUser.find(sq.toString(), list.toArray()).fetch();
            if (mList.size() > 0) returnFlag = "2";
        }

        return returnFlag;
    }

    /**
     * 创建用户信息
     *
     * @return
     */
    public boolean create() {
        Images.Captcha captcha = Images.captcha();
        String password_salt = captcha.getText(6);
        // 密码加密
        encryptedPassword = DigestUtils.md5Hex(encryptedPassword
                + password_salt);
        // 随机吗
        passwordSalt = password_salt;
        createdAt = new Date();
        lockVersion = 0;
        deleted = DeletedStatus.UN_DELETED;
        return super.create();
    }

    // FIXME: findAdmin这个名字，是指只找Admin用户？这个应该是findUser
    public static OperateUser findAdmin(String admin) {
        return find("byLoginName", admin).first();
    }

    public static OperateUser findUser(String loginName) {
        Logger.debug("loginName=" + loginName + " ^^^^^^^^^^^^^^^^");
        return OperateUser.find("byLoginName", loginName).first();
    }

    /**
     * 修改密码
     *
     * @param newUser  新用户
     * @param password 密码
     */
    public static void updatePassword(OperateUser newUser, String password) {
        // 随机码
        Images.Captcha captcha = Images.captcha();
        String newPasswordSalt = captcha.getText(6);
        newUser.passwordSalt = newPasswordSalt;
        // 新密码
        newUser.encryptedPassword = DigestUtils.md5Hex(password + newPasswordSalt);
        newUser.save();

    }


    public static List<OperateUser> getSales(String role) {
        List<OperateUser> operateUsers = OperateUser.find("select ou from OperateUser ou" +
                " where ou.id in (select ou.id from ou.roles r where r.key= ?) and deleted=?", role, DeletedStatus.UN_DELETED).fetch();
        return operateUsers;
    }

    public static OperateUser findByUserName(String userName) {
        return OperateUser.find("userName=? and deleted=?", userName, DeletedStatus.UN_DELETED).first();
    }

    public static OperateUser findBySalesId(Long salesId) {
        return OperateUser.find("id=? and deleted=?", salesId, DeletedStatus.UN_DELETED).first();
    }



}
