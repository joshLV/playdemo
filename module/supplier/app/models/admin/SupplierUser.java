package models.admin;

import com.uhuila.common.constants.DeletedStatus;
import models.sales.Shop;
import models.supplier.Supplier;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.Images;
import play.modules.paginate.JPAExtPaginator;
import play.modules.view_ext.annotation.Mobile;
import play.mvc.Http.Request;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "supplier_users")
public class SupplierUser extends Model {
    @Column(name = "login_name")
    @Required
    public String loginName;

    @Required
    @Mobile
    public String mobile;

    /**
     * 工号.
     * 用于短信验证方式，消费者向指定号码回复工号，即可完成验证。
     * TODO: 员工工号需要保证在同一个Supplier中唯一
     */
    @Column(name = "job_number")
    @Required
    @MaxSize(value = 6)
    public String jobNumber;

    @Column(name = "encrypted_password")
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
    public String userName;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    public Supplier supplier;

    @Transient
    public String confirmPassword;
    
    @Transient
    public String oldPassword;

    @OneToOne
    @JoinColumn(name = "shop_id", nullable = true)
    public Shop shop;

    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "supplier_users_roles",
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            joinColumns = @JoinColumn(name = "user_id"))
    @OrderBy("id")
    public List<SupplierRole> roles;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "supplier_permissions_users",
            inverseJoinColumns = @JoinColumn(name = "permission_id"),
            joinColumns = @JoinColumn(name = "user_id"))
    public Set<SupplierPermission> permissions;

    /**
     * 查询操作员信息
     *
     * @param loginName  用户名
     * @param pageNumber 页数
     * @param pageSize   记录数
     * @return 操作员信息
     */
    public static JPAExtPaginator<SupplierUser> getSupplierUserList(String loginName, Long supplierId,
                                                                    int pageNumber, int pageSize) {
        StringBuffer sql = new StringBuffer();
        Map params = new HashMap();
        sql.append("supplier.id = :supplierId");
        params.put("supplierId", supplierId);

        sql.append(" and deleted = :deleted ");
        params.put("deleted", DeletedStatus.UN_DELETED);

        if (StringUtils.isNotBlank(loginName)) {
            sql.append(" and loginName like :loginName");
            params.put("loginName", loginName + "%");
        }

        JPAExtPaginator<SupplierUser> usersPage = new JPAExtPaginator<>("SupplierUser s", "s",
                SupplierUser.class, sql.toString(), params).orderBy("createdAt desc");
        usersPage.setPageNumber(pageNumber);
        usersPage.setPageSize(pageSize);
        return usersPage;
    }

    /**
     * 更新操作员信息
     *
     * @param id           ID
     * @param supplierUser 用户信息
     */
    public static void update(long id, SupplierUser supplierUser) {

        SupplierUser updatedUser = SupplierUser.findById(id);
        updatedUser.roles = supplierUser.roles;
        updatedUser.loginName = supplierUser.loginName;
        updatedUser.userName = supplierUser.userName;
        updatedUser.mobile = supplierUser.mobile;
        updatedUser.shop = supplierUser == null ? null : supplierUser.shop;
        updatedUser.lastLoginAt = new Date();
        updatedUser.updatedAt = new Date();
        updatedUser.jobNumber = supplierUser.jobNumber;
        //获得IP
        updatedUser.lastLoginIP = Request.current().remoteAddress;

        updatedUser.save();
    }

    /**
     * 判断用户名和手机是否唯一
     *
     * @param loginName  用户名
     * @param mobile     手机
     * @param supplierId
     */
    public static String checkValue(Long id, String loginName, String mobile, String jobNumber, Long supplierId) {
        StringBuilder sq = new StringBuilder("loginName = ? and supplier=? ");
        List params = new ArrayList();
        params.add(loginName);
        params.add(new Supplier(supplierId));
        if (id != null) {
            sq.append("and id <> ?");
            params.add(id);
        }
        String returnFlag = "0";
        List<SupplierUser> supplierUserList = SupplierUser.find(sq.toString(), params.toArray()).fetch();

        //用户名存在的情况
        if (supplierUserList.size() > 0) return "1";

        sq = new StringBuilder("mobile = ? and supplier=? ");
        params = new ArrayList();
        params.add(mobile);
        params.add(new Supplier(supplierId));
        if (id != null) {
            sq.append("and id <> ?");
            params.add(id);
        }
        //手机存在的情况
        supplierUserList = SupplierUser.find(sq.toString(), params.toArray()).fetch();
        if (supplierUserList.size() > 0) return "2";

        //工号存在
        sq = new StringBuilder("jobNumber = ? and supplier=? ");
        params = new ArrayList();
        params.add(jobNumber);
        params.add(new Supplier(supplierId));
        if (id != null) {
            sq.append("and id <> ?");
            params.add(id);
        }
        supplierUserList = SupplierUser.find(sq.toString(), params.toArray()).fetch();
        if (supplierUserList.size() > 0) return "3";

        return "0";
    }

    /**
     * 创建用户信息
     *
     * @param supplierId
     * @return
     */
    public boolean create(Long supplierId) {
        Images.Captcha captcha = Images.captcha();
        Supplier supplier = Supplier.findById(supplierId);
        String password_salt = captcha.getText(6);
        // 密码加密
        encryptedPassword = DigestUtils.md5Hex(encryptedPassword
                + password_salt);
        // 随机码
        passwordSalt = password_salt;
        createdAt = new Date();
        lockVersion = 0;
        this.supplier = supplier;
        deleted = DeletedStatus.UN_DELETED;
        // 获得IP
        lastLoginIP = Request.current().remoteAddress;
        return super.create();
    }

    // FIXME: findAdmin这个名字，是指只找Admin用户？这个应该是findUser
    public static SupplierUser findAdmin(Long supplierId, String admin) {
        Supplier supplier = Supplier.findById(supplierId);
        return find("bySupplierAndLoginName", supplier, admin).first();
    }

    public static SupplierUser findUserByDomainName(String domainName, String loginName) {
        Supplier supplier = Supplier.find("byDomainName", domainName).first();
        if (supplier == null) {
            return null;
        }

        List<SupplierUser> all = SupplierUser.findAll();
        for (SupplierUser user : all) {
            Logger.debug("----- user.id:" + user.id + ", supplierId:" + user.supplier.id + ", loginName:" + user.loginName);
        }

        return SupplierUser.find("bySupplierAndLoginName", supplier, loginName).first();
    }

    /**
     * 修改密码
     *
     * @param newUser 新用户
     * @param user    原用户
     */
    public static void updatePassword(SupplierUser newUser, SupplierUser user) {
        // 随机码
        Images.Captcha captcha = Images.captcha();
        String newPasswordSalt = captcha.getText(6);
        newUser.passwordSalt = newPasswordSalt;
        // 新密码
        String newPassword = user.encryptedPassword;
        newUser.encryptedPassword = DigestUtils.md5Hex(newPassword + newPasswordSalt);
        newUser.save();

    }

    /**
     * 验证手机是否存在
     *
     * @param mobile
     * @return
     */
    public static boolean checkMobile(String mobile) {
        SupplierUser supplierUser = SupplierUser.find("mobile = ?", mobile).first();
        if (supplierUser != null) {
            return true;
        }
        return false;
    }

    /**
     * 找回密码，更新新密码
     *
     * @param supplierUserId
     * @param mobile
     * @param password
     */
    public static void updateFindPwd(Long supplierUserId, String mobile,
                                     String password) {
        SupplierUser supplierUser = SupplierUser.find("id = ? and mobile = ?", supplierUserId, mobile).first();
        if (supplierUser != null) {
            Images.Captcha captcha = Images.captcha();
            String password_salt = captcha.getText(6);
            // 密码加密
            supplierUser.encryptedPassword = DigestUtils.md5Hex(password
                    + password_salt);
            // 随机码
            supplierUser.passwordSalt = password_salt;
            supplierUser.save();
        }

    }

    /**
     * 查询操作员信息
     *
     * @param mobile   手机
     * @param supplier 商户ID
     * @return 操作员
     */
    public static SupplierUser findByMobileAndSupplier(String mobile, Supplier supplier) {
        SupplierUser supplierUser = SupplierUser.find("deleted = ? and mobile = ? and supplier = ?",
                DeletedStatus.UN_DELETED, mobile, supplier).first();
        return supplierUser;
    }

}
